package com.georgegarside.cryptovalise

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.Coin
import com.georgegarside.cryptovalise.model.NumberFormat
import com.georgegarside.cryptovalise.model.format
import com.georgegarside.cryptovalise.presenter.CoinRecyclerViewAdapter
import com.georgegarside.cryptovalise.presenter.ShareActionProviderLocation
import com.georgegarside.cryptovalise.presenter.setDeltaColour
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.android.synthetic.main.fragment_coin_detail.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback

/**
 * Fragment containing all coin details with cards for each property of the coin.
 */
class CoinDetailFragment : Fragment() {
	companion object {
		const val coinSymbolKey = "coin_symbol"
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_coin_detail, container, false).also {
				launch(UI) {
					// Get coin ID for its info to be displayed in this fragment
					val coinSymbol = arguments?.getString(coinSymbolKey, "") ?: ""
					
					// If there is no passed coin ID to the fragment, finish the activity and return
					if (coinSymbol.isBlank()) {
						return@launch
					}
					
					loadData(it, coinSymbol)
				}
			}
	
	/**
	 * Load a given [symbol]'s data into the [view].
	 */
	suspend fun loadData(view: View, symbol: String) {
		val coin = API.coins.await()[symbol] ?: run {
			activity?.onBackPressed()
			return
		}
		
		activity?.collapsingToolbar?.title = coin.name
		activity?.toolbarDetail?.title = coin.name
		
		// Set the share intent to current price of the coin and accompanying delta today
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			type = "text/plain"
			putExtra(
					Intent.EXTRA_TEXT,
					"${coin.name} is at ${coin.price.usdPrice}, " +
							"${coin.delta.day.first.format(NumberFormat.Normal, "%")} today"
			)
		}
		// Can't smart cast because activity has custom getter
		(activity as? ShareActionProviderLocation)?.defineShareIntent(shareIntent)
		
		with(view) {
			
			// Load prices
			CoinRecyclerViewAdapter.loadPrices(this, symbol)
			
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
			coinDescriptionMore.setOnClickListener(showDescription(coin))
			
			// Coin logo asynchronous
			launch(UI) {
				val logo = coin.logo.await() ?: return@launch
				coinLogo.setImageBitmap(logo)
				coinLogoCopy.setOnClickListener(copyLogo(coin))
			}
			
			// Coin market cap
			capDelta.text = coin.delta.cap.first.format(NumberFormat.Delta, "%")
			capDelta.setDeltaColour()
			capTotal.text = coin.delta.cap.second.format()
			
			// Coin volume
			volDelta.text = coin.delta.vol.first.format(NumberFormat.Delta, "%")
			volDelta.setDeltaColour()
			volTotal.text = coin.delta.vol.second.format()
			
			// Coin supply
			supply.text = getString(R.string.coin_detail_supply, coin.supply.format())
			if (coin.total == 0L) {
				supplyTotal.text = getString(R.string.coin_detail_supply_total_unlimited)
			} else {
				supplyTotal.text = getString(R.string.coin_detail_supply_total, coin.total.format())
			}
			
			// Coin dominance
			domDelta.text = coin.delta.dom.first.format(NumberFormat.Delta, "%")
			domDelta.setDeltaColour()
			rank.text = getString(R.string.coin_detail_rank, coin.delta.dom.second)
			
			// Coin links
			coin.links.website?.let { uri ->
				websiteDomain.text = uri.host
				launch(UI) {
					val colour = CoinRecyclerViewAdapter.getLogoColour(symbol)
					websiteVisit.setOnClickListener { openUri(uri, colour) }
				}
			} ?: run {
				websiteDomain.text = context.getString(R.string.coin_detail_links_none)
				websiteVisit.visibility = View.GONE
			}
			
			coin.links.whitepaper?.let { uri ->
				// Resets for tablet
				whitepaperVisit.apply {
					text = context.getString(R.string.coin_detail_links_whitepaper)
					setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
					isClickable = true
				}
				launch(UI) {
					val colour = CoinRecyclerViewAdapter.getLogoColour(symbol)
					whitepaperVisit.setOnClickListener { openUri(uri, colour) }
				}
			} ?: whitepaperVisit.apply {
				text = context.getString(R.string.coin_detail_links_none)
				setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
				isClickable = false
			}
		}
		
		activity?.supportStartPostponedEnterTransition()
	}
	
	/**
	 * Copy a [coin]'s logo on to the clipboard. Requires a [Coin.logoPath] at which the logo can be retrieved.
	 */
	private fun copyLogo(coin: Coin): (View) -> Unit = {
		val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clipUri = ClipData.newRawUri("Logo for ${coin.name}", Uri.parse(coin.logoPath))
		clipboard.primaryClip = clipUri
		Snackbar.make(it, R.string.coin_detail_copy_logo_done, Snackbar.LENGTH_SHORT).show()
	}
	
	/**
	 * Show a dialog containing the [Coin.description] of the [coin].
	 */
	private fun showDescription(coin: Coin): (View) -> Unit = { view ->
		AlertDialog.Builder(view.context).apply {
			setMessage(coin.description?.replace(". ", ".\n\n"))
			setNeutralButton(getString(R.string.close), { dialog, _ -> dialog.dismiss() })
			show()
		}
	}
	
	/**
	 * Open a [uri] and set the toolbar [colour] of the tab on opening the page.
	 */
	private fun openUri(uri: Uri, colour: Int? = null) {
		// If this fragment is no longer on screen, do not perform an action
		if (context == null) return
		
		val intent = CustomTabsIntent.Builder()
				.setToolbarColor(colour ?: ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
				.addDefaultShareMenuItem()
				.setShowTitle(true)
				.setInstantAppsEnabled(true)
				.build()
		
		// Open tab in a custom Google Chrome tab using an intent, with a WebView within the app if Chrome is not found
		CustomTabsHelper.openCustomTab(context!!, intent, uri, WebViewFallback())
	}
}
