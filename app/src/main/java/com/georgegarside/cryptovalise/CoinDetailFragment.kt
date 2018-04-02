package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.android.synthetic.main.activity_coin_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CoinDetailFragment : Fragment() {
	companion object {
		const val coinSymbolKey = "coin_symbol"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Get coin ID for its info to be displayed in this fragment
		val coinSymbol = arguments?.getString(coinSymbolKey, "")
		
		// If there is no passed coin ID to the fragment, finish the activity and return
		if (coinSymbol.isNullOrBlank()) {
			activity?.onBackPressed()
			return
		}
		
		launch(UI) {
			API.coins.await()[coinSymbol]
			activity?.collapsingToolbar?.title = coinSymbol
		}

/*
		arguments?.let {
			if (it.containsKey(coinSymbolKey)) {
				activity?.toolbar_layout?.title = it.getInt(coinSymbolKey).toString()
			}
		}
*/
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val rootView = inflater.inflate(R.layout.coin_detail, container, false)
		
/*
		mItem?.let {
			rootView.coin_detail.text = it.details
		}
*/
		
		return rootView
	}
}
