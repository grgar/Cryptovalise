package com.georgegarside.cryptovalise

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.CoinsContentProvider
import com.georgegarside.cryptovalise.model.DBOpenHelper
import com.georgegarside.cryptovalise.presenter.CurrencyRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_currency_list.*
import kotlinx.android.synthetic.main.currency_list.*
import kotlinx.android.synthetic.main.currency_list_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.android.synthetic.main.activity_currency_list.currencyList as currencyListActivity

/**
 * The main activity of the app which loads all the coins and data
 */
class CurrencyListActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
	
	/**
	 * Is screen showing both master and detail containers (true on tablet-scale containers)
	 */
	private var isMasterDetail = false
	
	/**
	 * [RecyclerView.Adapter] for the list of coins from the [CoinsContentProvider]
	 */
	private lateinit var adapter: CurrencyRecyclerViewAdapter
	
	/**
	 * Set up the activity
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_currency_list)
		
		setSupportActionBar(toolbar)
		
		// On large layout, detail is shown beside master
		isMasterDetail = currencyDetail != null
		
		// Bind the adapter to the recycler
		adapter = CurrencyRecyclerViewAdapter(this, isMasterDetail)
		currencyRecycler.adapter = adapter
		
		// Initialise the loader
		supportLoaderManager.initLoader(0, null, this)
		
		// Swipe to refresh is implemented to reload the prices
		currencyList.setOnRefreshListener(refreshListener)
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
		0 -> CursorLoader(this, CoinsContentProvider.Operation.ALL.uri,
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
	 * Clear any previously loaded data in the [adapter] for the [loader] which was reset
	 */
	override fun onLoaderReset(loader: Loader<Cursor>) = when (loader.id) {
		0 -> adapter.swapCursor(null)
		
		else -> {
		} // Ignore
	}
	
	/**
	 * Swipe to refresh is implemented to allow the user to manually reload the current market price for each coin
	 */
	private val refreshListener = {
		// Invalidate all previously received prices to make sure the prices are the latest ones available
		API.invalidateCache()
		
		// Only need to refresh views which exist, since views yet to exist haven't had data bound and will be making
		// their own request for the latest prices individually when inflated
		currencyRecycler.childViews().forEach {
			
			// Each loading of prices is performed asynchronously using dispatched coroutines
			launch(UI) {
				// Perform the API call to get the latest prices for the specific coin in question
				adapter.loadPrices(it, it.symbol.text.toString()).join()
				// Once the first data begins to come in, hide the loading indicator
				// Each card now has its own progress bar, so the indefinite indicator is extraneous at this point
				currencyList.isRefreshing = false
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
					R.id.add_currency -> {
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
			val coins = API.coins.await() as MutableMap
			
			val cursor = contentResolver.query(
					// Get all coins
					CoinsContentProvider.Operation.ALL.uri,
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
					dialog.setMessage(getString(R.string.add_coins_emptymsg))
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
	 * Add a coin to the user saved list of coins
	 */
	private fun addCoin(coin: API.Coin) {
		// Get the coin from the given symbol
		val uri = contentResolver.insert(CoinsContentProvider.Operation.ALL.uri, ContentValues().apply {
			put(DBOpenHelper.Coin.ID.toString(), coin.id)
			put(DBOpenHelper.Coin.Symbol.toString(), coin.symbol)
			put(DBOpenHelper.Coin.Name.toString(), coin.name)
		})
		supportLoaderManager.restartLoader(0, null, this@CurrencyListActivity)
		
		@SuppressLint("Recycle") // Android Studio bug, lint does not see `with` block as calling close on cursor
		val cursor = contentResolver.query(
				CoinsContentProvider.Operation.ALL.uri,
				// Get IDs
				arrayOf(DBOpenHelper.Coin.ID.toString()),
				null, null, null)
		
		val coinId = uri.lastPathSegment.toInt()
		
		val position = with(cursor.apply { moveToFirst(); moveToPrevious() }) {
			while (moveToNext()) {
				if (coinId == getInt(0)) break
			}
			close()
			position
		}
		
		adapter.notifyItemInserted(position)
		currencyRecycler.scrollToPosition(position)
	}
	
	/**
	 * Remove a [API.Coin] from the list of coins given the [index] of the coin in the list
	 */
	private fun removeCoin(index: Int) {
		TODO("Implement removing coin")
	}
}
