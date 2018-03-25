package com.georgegarside.cryptovalise

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.CoinsContentProvider
import com.georgegarside.cryptovalise.model.CustomLoader
import com.georgegarside.cryptovalise.model.DBOpenHelper
import com.georgegarside.cryptovalise.presenter.CurrencyRecyclerViewAdapter
import com.georgegarside.cryptovalise.presenter.CustomAnimation
import com.georgegarside.cryptovalise.presenter.progressAnimate
import kotlinx.android.synthetic.main.activity_currency_list.*
import kotlinx.android.synthetic.main.currency_list.*
import kotlinx.android.synthetic.main.currency_list.view.*
import kotlinx.android.synthetic.main.currency_list_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.android.synthetic.main.activity_currency_list.currencyList as currencyListActivity

class CurrencyListActivity : AppCompatActivity() {
	
	/**
	 * Is screen showing both master and detail containers (true on tablet-scale containers)
	 */
	private var isMasterDetail = false
	
	private val coinsUri = Uri.withAppendedPath(CoinsContentProvider.base, "coin")
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_currency_list)
		
		setSupportActionBar(toolbar)
		
		// On large layout, detail is shown beside master
		isMasterDetail = currencyDetail != null
		
		val table = DBOpenHelper.findTable("coin")
		
		val cursor = contentResolver.query(coinsUri, table?.columns,
				null, null, null, null)
		
		val adapter = CurrencyRecyclerViewAdapter(cursor, this, isMasterDetail)
		currencyList.currencyRecycler.adapter = adapter
		
		supportLoaderManager.initLoader(0, null,
				CustomLoader(this, coinsUri, adapter.cursorAdapter))
		
		currencyList.setOnRefreshListener {
			API.refreshPrices()
			currencyList.currencyRecycler.childViews().forEach {
				it.progressBar.progress = 0
				it.progressBar.animation = CustomAnimation.fadeIn
				launch(UI) {
					adapter.loadPrices(it, it.symbol.text.toString()).join()
					it.progressBar.progressAnimate(100)
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
	
	private val showAddCoinDialog = View.OnClickListener { view: View? ->
		with(AlertDialog.Builder(this)) {
			setTitle("Choose a coin")
			async(UI) {
				val coins = API.coins.await()
				setItems(coins.keys.toTypedArray(), { dialog, which ->
					if (view != null) {
						Snackbar.make(view, "Clicked item $which", Snackbar.LENGTH_LONG).show()
					}
					dialog.dismiss()
				})
				setNegativeButton("Cancel", { dialog, _ -> dialog.cancel() })
				show()
			}
		}
	}
}
