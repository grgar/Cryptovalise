package com.georgegarside.cryptovalise.presenter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.georgegarside.cryptovalise.CurrencyDetailActivity
import com.georgegarside.cryptovalise.CurrencyDetailFragment
import com.georgegarside.cryptovalise.CurrencyListActivity
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.Animation
import com.georgegarside.cryptovalise.model.replace
import kotlinx.android.synthetic.main.currency_list_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CurrencyRecyclerViewAdapter(private val cursor: Cursor,
                                  private val activity: CurrencyListActivity,
                                  private val isMasterDetail: Boolean) :
		RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder>() {
	
	val cursorAdapter = object : CursorAdapter(activity, cursor, 0) {
		// No implementation since view management is performed with ViewHolder
		override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View = TODO("Implement newView")
		
		override fun bindView(view: View, context: Context, cursor: Cursor) = TODO("Implement bindView")
	}
	
	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context)
				.inflate(R.layout.currency_list_content, parent, false))
		
		fun setData(cursor: Cursor) {
			with(cursor) {
				// Load basic info from database
				val symbol = getString(getColumnIndex("symbol"))
				view.symbol.text = symbol
				view.coinName.text = getString(getColumnIndex("name"))
				
				// Set click listeners
				view.buttonInfo.setOnClickListener(infoClickListener)
				
				// Load latest price info from API
				launch(UI) {
					API.coins.await()[symbol]?.let {
						view.priceDollars.text = it.price.usdPrice
						view.priceDollars.animation = Animation.fadeIn
						view.pricePounds.text = it.price.gbpPrice.await()
						view.pricePounds.animation = Animation.fadeIn
					}
				}
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
	
	private val infoClickListener by lazy {
		View.OnClickListener {
			if (isMasterDetail) {
				val fragment = CurrencyDetailFragment().apply {
					arguments = Bundle().apply {
						putString(CurrencyDetailFragment.ARG_ITEM_ID, "1")
					}
				}
				activity.replace(R.id.currencyDetail, fragment)
			} else {
				activity.startActivity(Intent(activity, CurrencyDetailActivity::class.java))
			}
		}
	}
}
