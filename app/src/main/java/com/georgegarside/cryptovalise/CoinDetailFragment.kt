package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_currency_detail.*
import kotlinx.android.synthetic.main.currency_detail.view.*

class CoinDetailFragment : Fragment() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		arguments?.let {
			if (it.containsKey(intentIdKey)) {
				activity?.toolbar_layout?.title = it.getInt(intentIdKey).toString()
			}
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val rootView = inflater.inflate(R.layout.currency_detail, container, false)
		
/*
		mItem?.let {
			rootView.currency_detail.text = it.details
		}
*/
		
		return rootView
	}
	
	companion object {
		const val intentIdKey = "item_id"
	}
}
