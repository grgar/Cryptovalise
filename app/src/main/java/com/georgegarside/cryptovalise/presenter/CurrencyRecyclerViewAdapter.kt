package com.georgegarside.cryptovalise.presenter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.georgegarside.cryptovalise.CurrencyDetailActivity
import com.georgegarside.cryptovalise.CurrencyDetailFragment
import com.georgegarside.cryptovalise.CurrencyListActivity
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.API
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
				// Inflate a new view based on the card layout defined in currency list content layout
				.inflate(R.layout.currency_list_content, parent, false))
		
		fun setData(cursor: Cursor) {
			view.progressBar.progressAnimate(10)
			// Load basic info from database
			val symbol = cursor.getString(cursor.getColumnIndex("symbol"))
			view.symbol.text = symbol
			view.coinName.text = cursor.getString(cursor.getColumnIndex("name"))
			
			// Set click listeners
			view.buttonInfo.setOnClickListener(infoClickListener)
			
			val deltaUp by lazy { ContextCompat.getColor(activity, R.color.deltaUp) }
			val deltaDown by lazy { ContextCompat.getColor(activity, R.color.deltaDown) }
			fun TextView.setDeltaColour() {
				setTextColor(if (text.startsWith("↓")) deltaDown else deltaUp)
			}
			
			view.progressBar.progressAnimate(20)
			
			// Load latest price info from API
			launch(UI) {
				API.coins.await()[symbol]?.let {
					view.priceDollars.fadeInText(it.price.usdPrice)
					view.progressBar.progressAnimate(50)
					// Deltas
					with(view.delta1h) {
						fadeInText(it.delta.sumHour, view.deltaHeader1h)
						setDeltaColour()
					}
					with(view.delta24h) {
						fadeInText(it.delta.sumDay, view.deltaHeader24h)
						setDeltaColour()
					}
					with(view.delta7d) {
						fadeInText(it.delta.sumWeek, view.deltaHeader7d)
						setDeltaColour()
					}
					
					// Pounds
					view.pricePounds.fadeInText(it.price.gbpPrice.await())
					view.progressBar.progressAnimate(100)
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
