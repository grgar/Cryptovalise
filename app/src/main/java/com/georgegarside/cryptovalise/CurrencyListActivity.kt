package com.georgegarside.cryptovalise

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

class CurrencyListActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
	
	/**
	 * Is screen showing both master and detail containers (true on tablet-scale containers)
	 */
	private var isMasterDetail = false
	
	private lateinit var adapter: CurrencyRecyclerViewAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_currency_list)
		
		setSupportActionBar(toolbar)
		
		// On large layout, detail is shown beside master
		isMasterDetail = currencyDetail != null
		
		adapter = CurrencyRecyclerViewAdapter(this, isMasterDetail)
		currencyRecycler.adapter = adapter
		
		supportLoaderManager.initLoader(0, null, this)
		//loader = CustomLoader(this, CoinsContentProvider.Operation.ALL.uri, adapter.cursorAdapter)
		
		currencyList.setOnRefreshListener {
			API.refreshPrices()
			currencyRecycler.childViews().forEach {
				launch(UI) {
					adapter.loadPrices(it, it.symbol.text.toString()).join()
					currencyList.isRefreshing = false
				}
			}
		}
	}
	
	private fun RecyclerView.childViews() = object : Iterator<View> {
		private var currentIndex = 0
		
		override fun hasNext(): Boolean = childCount > currentIndex
		
		override fun next(): View = getChildAt(currentIndex++)
	}
	
	override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> = when (id) {
		0 -> CursorLoader(this, CoinsContentProvider.Operation.ALL.uri,
				null, null, null, null)
		
		else -> throw Exception("Invalid loader ID")
	}
	
	override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) = when (loader.id) {
		0 -> adapter.swapCursor(data)
		
		else -> throw Exception("Invalid loader ID")
	}
	
	override fun onLoaderReset(loader: Loader<Cursor>) = when (loader.id) {
		0 -> adapter.swapCursor(null)
		
		else -> {
		} // Ignore
	}
	
	/**
	 * Inflate a menu into the toolbar, and set click listener for menu items
	 */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (isMasterDetail) {
			menuInflater.inflate(R.menu.toolbar, menu)
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
			fab?.setOnClickListener(showAddCoinDialog)
		}
		return super.onCreateOptionsMenu(menu)
	}
	
	private val showAddCoinDialog = View.OnClickListener { view: View ->
		val dialog = AlertDialog.Builder(this).apply {
			setTitle(getString(R.string.add_coin_title))
		}
		
		async(UI) {
			val coins = API.coins.await() as MutableMap
			
			// Remove any coins from the list to be added if they already exist in the added list
			val cursor = contentResolver.query(
					// Get all coins
					CoinsContentProvider.Operation.ALL.uri,
					// Get symbol column
					arrayOf(DBOpenHelper.Coin.Symbol.column),
					// Basic catch-all query
					null, null, null)
					// Begin from first row
					.apply { moveToFirst() }
			generateSequence(cursor.apply { moveToPrevious() }) {
				if (cursor.moveToNext()) cursor else null
			}.forEach {
				val symbol = it.getString(0)
				if (coins[symbol] != null) coins.remove(symbol)
			}
			
			with(dialog) {
				if (coins.isEmpty()) {
					dialog.setMessage(getString(R.string.add_coins_emptymsg))
				} else {
					val coinsArray = List(coins.size, {
						coins.keys.toTypedArray()[it] + " - " + coins.values.toTypedArray()[it].name
					})
					dialog.setItems(coinsArray.toTypedArray(), { dialog, which ->
						
						val coin = coins[coinsArray[which].split(" - ").first()] ?: return@setItems
						addCoin(coin.symbol)
						
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
	
	private fun addCoin(symbol: String) = launch(UI) {
		val coin = API.coins.await()[symbol] ?: return@launch
		contentResolver.insert(CoinsContentProvider.Operation.ALL.uri, ContentValues().apply {
			put(DBOpenHelper.Coin.Symbol.column, coin.symbol)
			put(DBOpenHelper.Coin.Name.column, coin.name)
		})
		supportLoaderManager.restartLoader(0, null, this@CurrencyListActivity)
		
		adapter.notifyItemInserted(adapter.itemCount - 1)
		currencyRecycler.smoothScrollToPosition(adapter.itemCount)
	}
	
	private fun removeCoin(index: Int) {
		adapter.notifyItemInserted(index)
	}
}
