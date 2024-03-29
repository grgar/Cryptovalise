package com.georgegarside.cryptovalise.presenter

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.georgegarside.cryptovalise.CoinListActivity
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.*
import kotlinx.android.synthetic.main.coin_list_content.view.*
import kotlinx.android.synthetic.main.include_coin_prices.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.apply
import kotlin.arrayOf
import kotlin.let
import kotlin.to
import kotlin.with
import android.support.v4.util.Pair as SupportPair

/**
 * CoinRecyclerViewAdapter is a completely custom implementation of [RecyclerView.Adapter], necessary to implement the
 * [CursorAdapter] for a [RecyclerView]. This app's main database content is presented in cards in a RecyclerView
 * instead of a ListView, for a number of reasons:
 *
 * - increased performance and better support for dealing with the complex layout defined in this app for the coin card
 *   (each card is an inflation of [R.layout.coin_list_content])
 *
 * - [RecyclerView.ViewHolder] for maintaining access to a single view once asynchronous code has completed. This is
 *   crucial for implementing the more advanced content where live data is being streamed from a server and placed
 *   alongside content from the [CoinContentProvider] which purely accesses the database and not the [API].
 *
 * A ListView would have been much easier to implement: unlike a ListView, a [RecyclerView.Adapter] does not provide
 * an implementation for a [Cursor] with a [CursorAdapter]. Therefore I created this custom [CoinRecyclerViewAdapter],
 * to ensure I did not lose the features I wished to obtain from the superior RecyclerView. You can read more about
 * my development process for this adapter in my report.
 *
 * However, just because I needed to implement a custom adapter for the RecyclerView, this did not mean that I should
 * write code to handle cursors and loaders myself. Research I did into this topic lead to a number of Stack Overflow
 * answers, ranging from copy-pasting and modifying code used in the CursorAdapter and ListView adapters with extra
 * code to handle the Cursor, to re-implementing loader behaviour in this adapter. Instead of these, I chose to create
 * a CursorAdapter within my RecyclerView adapter, and could therefore use all the functionality which comes with this
 * with the cursor I could obtain from the loader. This includes automatically closing the Cursor when it is done with,
 * to automatically updating the list of cards when the data set changes, and only loading changes in these cases. This
 * increases the mental complexity of this code through the use of an additional class instance, however the benefits
 * this provides outweighs the ‘thought cost’ of such an implementation through improvements and functionality now
 * available with this.
 */
class CoinRecyclerViewAdapter(private val context: Context) :
		RecyclerView.Adapter<CoinRecyclerViewAdapter.ViewHolder>() {
	
	/**
	 * An instance of the [CursorAdapter], calling upon [CursorAdapter.newView] to inflate a new
	 * [R.layout.coin_list_content], and delegating the behaviour of [CursorAdapter.bindView] to the custom [ViewHolder]
	 * implementation of [RecyclerView.ViewHolder].
	 */
	private val cursorAdapter = object : CursorAdapter(context, null, 0) {
		
		/**
		 * Inflate a new view in the [context] and [parent] provided.
		 *
		 * [cursor] is unused in the initial inflation: only when it is necessary to [bindView] is the [Cursor] used. It is
		 * possible and permitted for the cursor to have changed between this method and the data being bound to the view.
		 */
		override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View =
				LayoutInflater.from(parent.context)
						// Inflate a new view based on the card layout defined in currency list content layout
						.inflate(R.layout.coin_list_content, parent, false)
		
		/**
		 * Bind the data in the [cursor] to the [view].
		 */
		override fun bindView(view: View, context: Context, cursor: Cursor) {
			// Load basic info from database
			val id = cursor.getInt(DBOpenHelper.Coin.ID.ordinal)
			val name = cursor.getString(DBOpenHelper.Coin.Name.ordinal)
			val symbol = cursor.getString(DBOpenHelper.Coin.Symbol.ordinal)
			
			view.apply {
				this.symbol.text = symbol
				coinName.text = name
				
				// BTC is always visible in the list
				buttonMore?.visibility = if (symbol == "BTC") View.INVISIBLE else View.VISIBLE
				
				// Set on click listeners
				setOnClickListener {
					if (coinPrices.priceDollars.text.isBlank()) return@setOnClickListener;
					val transitionElements = arrayOf<SupportPair<View, String>>(
							SupportPair(icon, resources.getString(R.string.transition_title)),
							SupportPair(coinPrices, resources.getString(R.string.transition_content))
					)
					(this@CoinRecyclerViewAdapter.context as? CoinListActivity)?.openInfo(symbol, transitionElements)
				}
				
				(context as? CoinListActivity)?.apply {
					buttonMore.setOnClickListener { showCoinMenu(buttonMore, id) }
				}
			}
			
			// These methods to load additional data are asynchronous and run simultaneously
			loadPrices(view, symbol)
			loadLogo(view, symbol)
		}
	}
	
	/**
	 * Swap the [cursor] within the [cursorAdapter] for a new one. This method will notify the RecyclerView that the data
	 * set has changed in its entirety.
	 */
	fun swapCursor(cursor: Cursor?) {
		cursorAdapter.swapCursor(cursor)
		notifyDataSetChanged()
	}
	
	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
	
	// Documentation inherited
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
			ViewHolder(cursorAdapter.newView(context, cursorAdapter.cursor, parent))
	
	// Documentation inherited
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (cursorAdapter.cursor.isClosed) return
		cursorAdapter.cursor.moveToPosition(position)
		cursorAdapter.bindView(holder.itemView, context, cursorAdapter.cursor)
	}
	
	// Documentation inherited
	override fun getItemCount(): Int = cursorAdapter.count
	
	companion object DataBinder {
		/**
		 * Get a [Bitmap] logo for the given [Coin] [symbol].
		 */
		private suspend fun getLogo(symbol: String): Bitmap? = API.coins.await()[symbol]?.logo?.await()
		
		/**
		 * Calculate the dominant [Palette.Swatch] RGB from the coin's [getLogo]. Returns the swatch as RGB.
		 */
		suspend fun getLogoColour(coinSymbol: String): Int? {
			val logo = getLogo(coinSymbol) ?: return null
			return Palette.from(logo).generate().dominantSwatch?.rgb
		}
		
		/**
		 * Load the latest price information for the coin with [symbol] in view from the [API] into the [view]. The view must
		 * be an inflation of [R.layout.coin_list_content] for the view data to be bound into the correct locations. This
		 * method runs a coroutine in the [UI] handler context, so this runs asynchronously to other coin cards being loaded.
		 */
		fun loadPrices(view: View, symbol: String) = launch(UI) {
			// Get the coin whose data this method will load into the view
			val coin = API.coins.await()[symbol] ?: return@launch
			
			// Confirm the view we're binding to has the correct symbol for the data to be inserted
			view.symbol?.let {
				if (it.text != symbol) return@launch
			}
			
			with(view) {
				
				// Price in Dollars
				priceDollars?.fadeInText(coin.price.usdPrice)
						// If there is no price dollars view, don't continue loading other prices
						?: return@launch
				
				// Price deltas
				mapOf(
						coin.delta.hour.first to Pair(delta1h, deltaHeader1h),
						coin.delta.day.first to Pair(delta24h, deltaHeader24h),
						coin.delta.week.first to Pair(delta7d, deltaHeader7d)
				).forEach {
					it.value.first.apply {
						fadeInText(it.key.format(NumberFormat.Delta), it.value.second)
						setDeltaColour()
					}
				}
				
				progressBar?.progressAnimate(35)
				
				// Price in Pounds
				pricePounds.fadeInText(coin.price.gbpPrice.await())
				progressBar?.progressAnimate(35)
			}
		}
		
		/**
		 * Load the coin's logo into the [view].
		 */
		fun loadLogo(view: View, symbol: String) = launch(UI) {
			val logo = getLogo(symbol) ?: return@launch
			
			// After asynchronous operations, need to check whether the view has been bound to a different coin
			if (view.symbol.text != symbol) return@launch
			
			// The logo returned is a bitmap which can be placed directly into the view
			view.icon.setImageBitmap(logo)
			
			// Animation and progress update
			view.icon.animation = CustomAnimation.fadeIn
			view.progressBar.progressAnimate(100)
		}
	}
}
