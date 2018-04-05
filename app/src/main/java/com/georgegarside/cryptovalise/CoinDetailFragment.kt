package com.georgegarside.cryptovalise

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.NumberFormat
import com.georgegarside.cryptovalise.model.format
import com.georgegarside.cryptovalise.presenter.setDeltaColour
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.android.synthetic.main.coin_detail.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CoinDetailFragment : Fragment() {
	companion object {
		fun createFragment(bundle: Bundle) =
				CoinDetailFragment().apply {
					arguments = bundle
				}
		
		const val coinSymbolKey = "coin_symbol"
	}
	
	lateinit var coinSymbol: String
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Get coin ID for its info to be displayed in this fragment
		coinSymbol = arguments?.getString(coinSymbolKey, "") ?: ""
		
		// If there is no passed coin ID to the fragment, finish the activity and return
		if (coinSymbol.isBlank()) {
			activity?.onBackPressed()
			return
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.coin_detail, container, false).also {
				launch(UI) {
					loadData(it, coinSymbol)
				}
			}
	
	private suspend fun loadData(view: View, symbol: String) {
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
		view.coinDescription.text = shortDescription
		
		// Coin logo
		launch(UI) {
			val logo = coin.logo.await() ?: return@launch
			view.coinLogo.setImageBitmap(logo)
		}
		
		view.coinLogoCopy.setOnClickListener(copyLogo(coin))
		
		// Coin market cap
		view.capDelta.text = coin.delta.cap.first.format(NumberFormat.Delta, "%")
		view.capDelta.setDeltaColour()
		view.capTotal.text = coin.delta.cap.second.format()
		
		// Coin volume
		view.volDelta.text = coin.delta.vol.first.format(NumberFormat.Delta, "%")
		view.volDelta.setDeltaColour()
		view.volTotal.text = coin.delta.vol.second.format()
		
		// Coin supply
		view.supply.text = getString(R.string.coin_detail_supply, coin.supply.format())
		if (coin.total == 0L) {
			view.supplyTotal.text = getString(R.string.coin_detail_supply_total_unlimited)
		} else {
			view.supplyTotal.text = getString(R.string.coin_detail_supply_total, coin.total.format())
		}
		
		// Coin dominance
		view.domDelta.text = coin.delta.dom.first.format(NumberFormat.Delta, "%")
		view.domDelta.setDeltaColour()
		view.rank.text = getString(R.string.coin_detail_rank, coin.delta.dom.second)
	}
	
	private fun copyLogo(coin: API.Coin): (View) -> Unit = {
		val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clipUri = ClipData.newRawUri("Logo for ${coin.name}", Uri.parse(coin.logoPath))
		clipboard.primaryClip = clipUri
	}
}
