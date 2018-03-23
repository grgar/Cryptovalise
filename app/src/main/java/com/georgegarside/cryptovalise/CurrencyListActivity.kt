package com.georgegarside.cryptovalise

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.CoinsContentProvider
import com.georgegarside.cryptovalise.model.DBOpenHelper
import kotlinx.android.synthetic.main.activity_currency_list.*
import kotlinx.android.synthetic.main.currency_list.currencyList
import kotlinx.android.synthetic.main.currency_list_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.android.synthetic.main.activity_currency_list.view.currencyList as currencyActivity

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [CurrencyDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class CurrencyListActivity : AppCompatActivity() {
	
	/**
	 * Is screen showing both master and detail containers (true on tablet-scale containers)
	 */
	private var isMasterDetail = false
	
	private val coinsUri = Uri.withAppendedPath(CoinsContentProvider.base, "coin")
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_currency_list)
		
		toolbar.title = title
		setSupportActionBar(toolbar)
		
		//fab?.setOnClickListener(showAddCoinDialog)
		
		// Larger than res/values-w900dp, detail is shown beside master
		if (currencyDetail != null) {
			isMasterDetail = true
		}
		
		val table = DBOpenHelper.findTable("coin")
		
		val cursor = contentResolver.query(coinsUri, table?.columns,
				null, null, null, null)
		
		val adapter = CurrencyRecyclerViewAdapter(cursor, isMasterDetail)
		currencyList.adapter = adapter
		
		supportLoaderManager.initLoader(0, null, CurrencyLoader(this, adapter.cursorAdapter))
	}
	
	private val showAddCoinDialog: (View?) -> Unit = { view ->
		/*val intent = Intent(this, CurrencyDetailActivity::class.java)
		intent.putExtra(CurrencyDetailFragment.ARG_ITEM_ID, "1")
		startActivity(intent)
		Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()*/
		with(AlertDialog.Builder(this)) {
			setTitle("Choose a coin")
			async(UI) {
				val coins = API.coins.await()
				Log.i("gLog", coins.toString())
				setItems(coins.keys.toTypedArray(), { dialog, which ->
					if (view != null) {
						Snackbar.make(view, "Clicked item $which", Snackbar.LENGTH_LONG).show()
					}
					dialog.dismiss()
				})
				setNegativeButton("Cancel", { dialog, _ -> dialog.cancel() })
				show()
			}
			//show().listView
		}
	}
	
	private val infoClickListener: View.OnClickListener by lazy {
		View.OnClickListener {
			if (isMasterDetail) {
				val fragment = CurrencyDetailFragment().apply {
					arguments = Bundle().apply {
						putString(CurrencyDetailFragment.ARG_ITEM_ID, "1")
					}
				}
				supportFragmentManager.beginTransaction()
						.replace(R.id.currencyDetail, fragment)
						.commit()
			} else {
				startActivity(Intent(this, CurrencyDetailActivity::class.java))
			}
		}
	}
	
	inner class CurrencyRecyclerViewAdapter(private val cursor: Cursor,
	                                        private val isMasterDetail: Boolean) :
			RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder>() {
		
		val cursorAdapter = object : CursorAdapter(this@CurrencyListActivity, cursor, 0) {
			// No implementation since view management is performed with ViewHolder
			override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View = TODO("Implement newView")
			
			override fun bindView(view: View, context: Context, cursor: Cursor) = TODO("Implement bindView")
		}
		
		inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
			constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context)
					.inflate(R.layout.currency_list_content, parent, false))
			
			fun setData(cursor: Cursor) {
				with(cursor) {
					view.symbol.text = getString(getColumnIndex("symbol"))
					view.coinName.text = getString(getColumnIndex("name"))
					view.buttonInfo.setOnClickListener(infoClickListener)
				}
			}
		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
				ViewHolder(parent)
		
		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			cursor.moveToPosition(position)
			holder.setData(cursor)
		}
		
		override fun getItemCount(): Int = cursor.count
	}
	
	inner class CurrencyLoader(private val context: Context,
	                           private val cursorAdapter: CursorAdapter) : LoaderManager.LoaderCallbacks<Cursor> {
		override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
			return CursorLoader(context, coinsUri, null, null, null, null)
		}
		
		override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
			cursorAdapter.swapCursor(data)
		}
		
		override fun onLoaderReset(loader: Loader<Cursor>) {
			cursorAdapter.swapCursor(null)
		}
	}
}
