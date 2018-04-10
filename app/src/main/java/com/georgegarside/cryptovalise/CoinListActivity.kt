package com.georgegarside.cryptovalise

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.CoinContentProvider
import com.georgegarside.cryptovalise.model.DBOpenHelper
import com.georgegarside.cryptovalise.presenter.*
import kotlinx.android.synthetic.main.activity_coin_list.*
import kotlinx.android.synthetic.main.coin_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.android.synthetic.main.activity_coin_list.coinList as coinListActivity

/**
 * CoinListActivity is the main launcher activity for Cryptovalise which shows all of the coins the user has selected.
 * This activity's main view is a card-based vertical scrolling layout with a card for each coin added to the app by the
 * user. The user can add coins using the [fab] on mobile or from a menu option in [onCreateOptionsMenu] on tablet.
 *
 * The scrolling list is implemented using [RecyclerView] instead of a standard ListView. This is discussed in the
 * class documentation for the custom adapter I wrote: [CoinRecyclerViewAdapter].
 *
 * On tablet, this activity manages the loading of the master-detail layout, where the list of coins is shown in a
 * column on the left and the rest contains other content in the form of fragments, e.g. [CoinDetailFragment].
 *
 * Whether or not these components are displayed depending on mobile/tablet is dependent on the loading of the relevant
 * layout variation of [R.layout.activity_coin_list]. A large-land variation is used on tablet, which includes the
 * extra fragment content. This is used to determine the value of [isMasterDetail], used elsewhere in this class where
 * it is necessary to determine the difference between mobile or tablet.
 */
class CoinListActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
	
	/**
	 * Boolean for whether the screen is showing both master and detail containers (true on tablet-scale containers).
	 * Used by layout inflation and configuration to determine whether the layout has other fragments or content on the
	 * screen at the same time, and whether to use intents to other activities or to directly replace fragments.
	 */
	private var isMasterDetail = false
	
	/**
	 * A reference to a [RecyclerView.Adapter] for the list of coins from the [CoinContentProvider].
	 */
	private lateinit var adapter: CoinRecyclerViewAdapter
	
	/**
	 * The initial set up of the activity, where the [adapter] is set up and the loader is initialised.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_coin_list)
		
		setSupportActionBar(toolbar)
		
		// On large layout, detail is shown beside master
		isMasterDetail = coinDetail != null
		
		// Bind the adapter to the recycler
		adapter = CoinRecyclerViewAdapter(this, isMasterDetail)
		coinRecycler.adapter = adapter
		
		// Initialise the loader
		supportLoaderManager.initLoader(0, null, this)
		
		// Swipe to refresh is implemented to reload the prices
		coinList.setOnRefreshListener(refreshListener)
	}
	
	/**
	 * Extension function for recycler view, providing an iterator for all children which currently exist in the recycler
	 */
	private fun RecyclerView.childViews() = object : Iterator<View> {
		/**
		 * The current position of the iterator
		 */
		private var currentIndex = 0
		
		// These next methods inherit their documentation
		
		override fun hasNext(): Boolean = childCount > currentIndex
		
		override fun next(): View = getChildAt(currentIndex++)
	}
	
	/**
	 * Returns a new [Loader] for the given [id]
	 */
	override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> = when (id) {
		0 -> CursorLoader(this, CoinContentProvider.Operation.Coin.uri,
				null, null, null, null)
		
		else -> throw Exception("Invalid loader ID")
	}
	
	/**
	 * Once the [loader] has finished obtaining the data it needs, the [data] is passed to the relevant [adapter]
	 */
	override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) = when (loader.id) {
		0 -> adapter.swapCursor(data)
		
		else -> throw Exception("Invalid loader ID")
	}
	
	/**
	 * Clear any previously loaded data in the [adapter] for the [loader] which was reset.
	 * This method has no function, since we don't want to remove all the data added to the view even if the cursor should
	 * be reset. Super is not called since this is an implementation of an interface and therefore has no inherited super.
	 */
	override fun onLoaderReset(loader: Loader<Cursor>) = Unit
	
	/**
	 * Swipe to refresh is implemented to allow the user to manually reload the current market price for each coin.
	 * This only reloads content which is relevant to the user requesting a refresh and is most likely to have changed.
	 * For example, the coin's logo is not refreshed.
	 */
	private val refreshListener = {
		// Invalidate all previously received prices to make sure the obtained prices are the latest ones available
		API.invalidateCache()
		
		// Only need to refresh views which exist, since views yet to exist haven't had data bound and will be making
		// their own request for the latest prices individually when inflated
		coinRecycler.childViews().forEach {
			
			// Each card view is manipulated asynchronously using dispatched coroutines
			launch(UI) {
				// Refresh information for this position
				val i = coinRecycler.getChildAdapterPosition(it)
				adapter.notifyItemChanged(i)
				
				// Flash card with fast fade out-in
				it.animation = CustomAnimation.fadeIn
				
				// Once the first data begins to come in, hide the loading indicator
				// Each card now has its own progress bar, so the indefinite indicator is extraneous at this point
				coinList.isRefreshing = false
			}
		}
	}
	
	/**
	 * Segue to the coin info. On mobile, this starts an [Intent] to the [CoinDetailActivity] containing a
	 * [CoinDetailFragment]. On tablet, this directly replaces [R.id.coinDetail] with [CoinDetailFragment].
	 */
	fun openInfo(symbol: String) = async {
		val logoColour = CoinRecyclerViewAdapter.getLogoColour(symbol)
		
		// Create the bundle of data to be passed to the intent or fragment
		val bundle = Bundle().apply {
			// The coin's symbol is passed through for the info page to obtain the rest of the data using this key
			putString(CoinDetailFragment.coinSymbolKey, symbol)
			
			// Pass the logo colour to the activity so that it can style the toolbar
			logoColour?.let {
				putInt(CoinDetailActivity.coinColourKey, it)
			}
		}
		
		launch(UI) {
			// Determine whether to use fragments directly or start an activity
			if (isMasterDetail) {
				// Set details into fragment
				(coinDetail as? CoinDetailFragment)?.loadData(coinDetail?.view ?: return@launch, symbol)
				(chartFragment as? ChartFragment)?.apply {
					colour = logoColour?.let { rgbToSwatch(it) }?.also {
						window.setStatusBarColour(it.rgb)
					}
					loadChart(symbol, API.PriceSeries.Price)
				}
				logoColour?.let { toolbar.setColour(it) }
			} else {
				// Intent to detail activity
				val intent = Intent(this@CoinListActivity, CoinDetailActivity::class.java).apply {
					putExtras(bundle)
				}
				this@CoinListActivity.startActivity(intent)
			}
		}
	}
	
	/**
	 * Inflate a [menu] into the toolbar, and set click listener for menu items
	 */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		// Determine whether a menu is appropriate, which is only the case on tablet
		if (isMasterDetail) {
			
			// Inflate a menu defined in resources
			menuInflater.inflate(R.menu.toolbar, menu)
			
			// For each item in the menu, an appropriate click listener performs the associated action
			toolbar.setOnMenuItemClickListener {
				when (it.itemId) {
					R.id.menuAddCoin -> {
						showAddCoinDialog.onClick(toolbar.rootView)
						true
					}
					else -> false
				}
			}
		} else {
			// On mobile, the add coin button is a floating action button defined in the layout file
			fab?.setOnClickListener(showAddCoinDialog)
		}
		
		return super.onCreateOptionsMenu(menu)
	}
	
	/**
	 * Show a dialog for adding a coin to the list of coins.
	 * This dialog does not show coins which have already been added to the list.
	 */
	private val showAddCoinDialog = View.OnClickListener { view: View ->
		// Create a builder for the dialog and set the initial title
		val dialog = AlertDialog.Builder(this).apply {
			setTitle(getString(R.string.add_coin_title))
		}
		
		// Get list of coins to be displayed in coin dialog
		// Perform API and database query asynchronously in CommonPool, awaited for in UI pool to make UI changes
		val coins = async {
			// API call to get latest list of coins (cached by API object)
			val coins = API.coins.await().toMutableMap()
			
			val cursor = contentResolver.query(
					// Get all coins
					CoinContentProvider.Operation.Coin.uri,
					// Get symbol column
					arrayOf(DBOpenHelper.Coin.Symbol.toString()),
					// Basic catch-all query
					null, null, null)
			
			// Perform the set negation, removing coins if they already exist in the coins list
			with(cursor.apply { moveToFirst(); moveToPrevious() }) {
				while (moveToNext()) {
					// Column index is 0 since this is defined by the projection,
					// the column required will always be the only column returned
					// and its position is fixed by the referenced enumeration
					val symbol = getString(0)
					if (coins[symbol] != null) coins.remove(symbol)
				}
				close()
			}
			
			coins
		}
		
		// This asynchronous coroutine is performed in the UI pool, such that the closure is not directly performed on the
		// main thread, permitting suspension with await without blocking the main (UI) thread
		launch(UI) {
			// Get list of coins to be displayed in coin dialog
			// Perform this asynchronously
			val coinsMap = coins.await()
			// Convert map to list of strings for displaying in list
			val coinsArray = Array(coinsMap.size, {
				coinsMap.keys.toTypedArray()[it] + " " + coinsMap.values.toTypedArray()[it].name
			})
			
			with(dialog) {
				if (coinsArray.isEmpty()) {
					// Show a message that there are no more coins available to be added
					dialog.setMessage(getString(R.string.add_coin_emptymsg))
				} else {
					dialog.setItems(coinsArray, { dialog, which ->
						// Get the new coin which was selected by the user
						val coin = coinsMap[coinsArray[which].split(" ").first()] ?: return@setItems
						
						// Add the coin to the list of coins
						addCoin(coin)
						
						// Show notification and ability to undo
						Snackbar.make(view, "Added ${coin.name}", Snackbar.LENGTH_LONG).apply {
							setAction("Undo", {
								TODO("Perform undo")
							})
							show()
						}
						dialog.dismiss()
					})
				}
				
				setNegativeButton("Cancel", { dialog, _ -> dialog.cancel() })
				show()
			}
		}
	}
	
	/**
	 * Returns the index of the given [coinId] in the database.
	 */
	private fun rowIndexOfCoin(coinId: Int): Int {
		// Get a cursor of coin IDs in the database, to look for the coin
		@SuppressLint("Recycle") // Android Studio bug, lint does not see `with` block as calling close on cursor
		val cursor = contentResolver.query(
				CoinContentProvider.Operation.Coin.uri,
				// Get IDs
				arrayOf(DBOpenHelper.Coin.ID.toString()),
				null, null, null)
		
		// Determine the position the new coin was added into by moving cursor until row is found
		// The number of steps the cursor moved determines the position
		// This is a very lightweight procedure since the cursor's query has a projection limiting it to only the IDs
		// without any extraneous coin data that would go unused
		return with(cursor.apply { moveToFirst(); moveToPrevious() }) {
			// Continue until the coin is found
			while (moveToNext()) {
				// If the coin has been found, stop iterating the cursor at this line
				if (coinId == getInt(0)) break
			}
			// The cursor has completed so is closed here (aforementioned Android Studio IDE bug misses this in linting)
			close()
			// Get and return the cursor's current position
			position
		}
	}
	
	/**
	 * Add a [coin] to the user's saved list of coins. This method inserts the [API.Coin] into the database using the
	 * [CoinContentProvider], restarts the loader from [getSupportLoaderManager], informs the [adapter] that a coin has
	 * been inserted and scrolls the [coinRecycler] to the new coin to display it to the user.
	 */
	private fun addCoin(coin: API.Coin) {
		// Perform database insertion of coin
		val coinId = contentResolver.insert(CoinContentProvider.Operation.Coin.uri, ContentValues().apply {
			put(DBOpenHelper.Coin.ID.toString(), coin.id)
			put(DBOpenHelper.Coin.Symbol.toString(), coin.symbol)
			put(DBOpenHelper.Coin.Name.toString(), coin.name)
		}).lastPathSegment.toInt()
		
		// Loader needs to ensure it has loaded the latest data
		// In this case, the loader is a custom CursorLoader, so the cursor is replaced by this method
		// This method applies regardless of implementation though, so if the app replaces Cursor with something else in
		// the future, this method still applies (and without any changes) since the loader always needs to be restarted
		supportLoaderManager.restartLoader(0, null, this@CoinListActivity)
		
		/**
		 * Index of the coin's row in the database (and therefore adapter)
		 */
		val position = rowIndexOfCoin(coinId)
		
		launch(UI) {
			// Add row to recycler view
			adapter.notifyItemInserted(position)
			// Scroll to display the inserted coin (scrolls just enough for the coin to be visible, from either direction)
			coinRecycler.smoothScrollToPosition(position)
		}
	}
	
	/**
	 * Remove a [API.Coin] from the list of coins given the [coinId] of the [API.Coin] in the database.
	 */
	private fun removeCoin(coinId: Int) {
		/**
		 * The current index position of the coin to inform the adapter
		 */
		val position = rowIndexOfCoin(coinId)
		
		// Perform the deletion operation on the database
		contentResolver.delete(CoinContentProvider.Operation.Coin.uri,
				"${DBOpenHelper.Coin.ID} = ?", arrayOf(coinId.toString())).let {
			
			// If no coin was deleted, no need to continue with informing the UI
			if (it == 0) return
		}
		
		supportLoaderManager.restartLoader(0, null, this@CoinListActivity)
		
		launch(UI) {
			adapter.notifyItemRemoved(position)
		}
	}
	
	/**
	 * Show the [PopupMenu] for a [coinId]. The menu is attached to the [view] by inflating [R.menu.coin] and handling
	 * clicks for the given coin's menu.
	 */
	fun showCoinMenu(view: View, coinId: Int) = PopupMenu(this, view).apply {
		// Create a menu from layout
		inflate(R.menu.coin)
		
		// Handle clicking an option from the menu
		setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.menuDeleteCoin -> removeCoin(coinId)
			}
			true
		}
		
		show()
	}
}
