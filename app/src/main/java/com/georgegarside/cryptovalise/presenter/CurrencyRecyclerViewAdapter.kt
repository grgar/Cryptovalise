package com.georgegarside.cryptovalise.presenter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.georgegarside.cryptovalise.CurrencyDetailActivity
import com.georgegarside.cryptovalise.CurrencyDetailFragment
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.DBOpenHelper
import com.georgegarside.cryptovalise.model.replace
import kotlinx.android.synthetic.main.currency_list_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CurrencyRecyclerViewAdapter(private val context: Context,
                                  private val isMasterDetail: Boolean) :
		RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder>() {
	
	private val cursorAdapter = object : CursorAdapter(context, null, 0) {
		// No implementation since view management is performed with ViewHolder
		override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View =
				LayoutInflater.from(parent.context)
						// Inflate a new view based on the card layout defined in currency list content layout
						.inflate(R.layout.currency_list_content, parent, false)
		
		override fun bindView(view: View, context: Context, cursor: Cursor) =
				ViewHolder(view).setData(cursor)
	}
	
	fun swapCursor(cursor: Cursor?) {
		cursorAdapter.swapCursor(cursor)
		notifyDataSetChanged()
	}
	
	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		fun setData(cursor: Cursor) {
			// Load basic info from database
			val symbol = cursor.getString(DBOpenHelper.Coin.Symbol.ordinal)
			view.symbol.text = symbol
			view.coinName.text = cursor.getString(DBOpenHelper.Coin.Name.ordinal)
			
			// Set click listeners
			view.buttonInfo.setOnClickListener(infoClickListener)
			
			view.progressBar.progressAnimate(10)
			
			// These methods are asynchronous and run simultaneously
			loadPrices(view, symbol)
			loadLogo(view, symbol)
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
			ViewHolder(cursorAdapter.newView(context, cursorAdapter.cursor, parent))
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (cursorAdapter.cursor.isClosed) return
		cursorAdapter.cursor.moveToPosition(position)
		holder.setData(cursorAdapter.cursor)
	}
	
	override fun getItemCount(): Int = cursorAdapter.count
	
	private val deltaUp by lazy { ContextCompat.getColor(context, R.color.deltaUp) }
	private val deltaDown by lazy { ContextCompat.getColor(context, R.color.deltaDown) }
	private fun TextView.setDeltaColour() {
		setTextColor(if (text.startsWith("↓")) deltaDown else deltaUp)
	}
	
	// Load latest price info from API
	fun loadPrices(view: View, symbol: String) = launch(UI) {
		val coin = API.coins.await()[symbol] ?: return@launch
		if (view.symbol.text != symbol) return@launch
		
		view.priceDollars.fadeInText(coin.price.usdPrice)
		view.progressBar.progressAnimate(40)
		// Deltas
		with(view.delta1h) {
			fadeInText(coin.delta.sumHour, view.deltaHeader1h)
			setDeltaColour()
		}
		with(view.delta24h) {
			fadeInText(coin.delta.sumDay, view.deltaHeader24h)
			setDeltaColour()
		}
		with(view.delta7d) {
			fadeInText(coin.delta.sumWeek, view.deltaHeader7d)
			setDeltaColour()
		}
		
		// Pounds
		view.pricePounds.fadeInText(coin.price.gbpPrice.await())
		view.progressBar.progressAnimate(40)
	}
	
	fun loadLogo(view: View, symbol: String) = launch(UI) {
		val coin = API.coins.await()[symbol] ?: return@launch
		val logo = coin.logo.await() ?: return@launch
		
		// After asynchronous operations, need to check whether the view has been bound to a different coin
		if (view.symbol.text != coin.symbol) return@launch
		
		view.icon.setImageBitmap(logo)
		view.icon.animation = CustomAnimation.fadeIn
		view.progressBar.progressAnimate(100)
	}
	
	private val infoClickListener by lazy {
		View.OnClickListener {
			if (isMasterDetail) {
				val fragment = CurrencyDetailFragment().apply {
					arguments = Bundle().apply {
						putString(CurrencyDetailFragment.ARG_ITEM_ID, "1")
					}
				}
				(context as? FragmentActivity)?.replace(R.id.currencyDetail, fragment)
			} else {
				context.startActivity(Intent(context, CurrencyDetailActivity::class.java))
			}
		}
	}
}
