package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.android.synthetic.main.coin_detail.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CoinDetailFragment : Fragment() {
	companion object {
		const val coinSymbolKey = "coin_symbol"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Get coin ID for its info to be displayed in this fragment
		val coinSymbol = arguments?.getString(coinSymbolKey, "") ?: ""
		
		// If there is no passed coin ID to the fragment, finish the activity and return
		if (coinSymbol.isBlank()) {
			activity?.onBackPressed()
			return
		}
		
		launch(UI) {
			loadData(coinSymbol)
		}
	}
	
	private suspend fun loadData(symbol: String) {
		val coin = API.coins.await()[symbol] ?: run {
			activity?.onBackPressed()
			return
		}
		
		activity?.collapsingToolbar?.title = coin.name
		
		// Coin description abbreviated
		val shortDescription = coin.description
				?.split(" ")
				?.joinToString(
						separator = " ",
						prefix = "‘",
						postfix = "’",
						limit = 20,
						truncated = "…"
				)
				?: getString(R.string.coin_detail_missing_description)
		coinDescription.text = shortDescription
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
