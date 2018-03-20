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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.georgegarside.cryptovalise.dummy.DummyContent
import com.georgegarside.cryptovalise.model.CoinsContentProvider
import com.georgegarside.cryptovalise.model.DBOpenHelper
import kotlinx.android.synthetic.main.activity_currency_list.*
import kotlinx.android.synthetic.main.currency_list.*
import kotlinx.android.synthetic.main.currency_list_content.view.*

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
		
		fab.setOnClickListener {
			//startActivity(Intent(this, LoginActivity::class.java))
			Snackbar.make(it, "Replace with your own action", Snackbar.LENGTH_LONG)
					.setAction("Action", null).show()
		}
		
		// Larger than res/values-w900dp, detail is shown beside master
		if (currencyDetail != null) {
			isMasterDetail = true
		}
		
		val table = DBOpenHelper.findTable("coin")
		
		val cursor = contentResolver.query(coinsUri, table?.columns,
				null, null, null, null)
		
		val adapter = CurrencyRecyclerViewAdapter(this, cursor, isMasterDetail)
		currencyList.adapter = adapter
		
		supportLoaderManager.initLoader(0, null, CurrencyLoader(this, adapter.cursorAdapter))
	}
	
	class CurrencyRecyclerViewAdapter(private val context: Context,
	                                  private val cursor: Cursor,
	                                  private val isMasterDetail: Boolean) :
			RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder>() {
		
		val cursorAdapter = object : CursorAdapter(context, cursor, 0) {
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
	
	class SimpleItemRecyclerViewAdapter(private val mParentActivity: CurrencyListActivity,
	                                    private val mValues: List<DummyContent.DummyItem>,
	                                    private val mTwoPane: Boolean) :
			RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
		
		private val mOnClickListener: View.OnClickListener
		
		init {
			mOnClickListener = View.OnClickListener { v ->
				val item = v.tag as DummyContent.DummyItem
				if (mTwoPane) {
					val fragment = CurrencyDetailFragment().apply {
						arguments = Bundle().apply {
							putString(CurrencyDetailFragment.ARG_ITEM_ID, item.id)
						}
					}
					//mParentActivity.fragmentReplace(R.id.currencyDetail, fragment)
					mParentActivity.supportFragmentManager
							.beginTransaction()
							.replace(R.id.currencyDetail, fragment)
							.commit()
				} else {
					val intent = Intent(v.context, CurrencyDetailActivity::class.java).apply {
						putExtra(CurrencyDetailFragment.ARG_ITEM_ID, item.id)
					}
					v.context.startActivity(intent)
				}
			}
		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val view = LayoutInflater.from(parent.context)
					.inflate(R.layout.currency_list_content, parent, false)
			return ViewHolder(view)
		}
		
		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val item = mValues[position]
			//holder.mIdView.text = item.id
			//holder.mContentView.text = item.content
			
			with(holder.itemView) {
				tag = item
				setOnClickListener(mOnClickListener)
			}
		}
		
		override fun getItemCount(): Int {
			return mValues.size
		}
		
		inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
			//val mIdView: TextView = mView.id_text
			//val mContentView: TextView = mView.content
		}
	}
}
