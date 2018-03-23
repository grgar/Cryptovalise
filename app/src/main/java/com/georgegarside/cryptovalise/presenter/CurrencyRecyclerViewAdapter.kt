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
import kotlinx.android.synthetic.main.currency_list_content.view.*

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
	
	private val infoClickListener by lazy {
		View.OnClickListener {
			if (isMasterDetail) {
				val fragment = CurrencyDetailFragment().apply {
					arguments = Bundle().apply {
						putString(CurrencyDetailFragment.ARG_ITEM_ID, "1")
					}
				}
				activity.supportFragmentManager.beginTransaction()
						.replace(R.id.currencyDetail, fragment)
						.commit()
			} else {
				activity.startActivity(Intent(activity, CurrencyDetailActivity::class.java))
			}
		}
	}
}
