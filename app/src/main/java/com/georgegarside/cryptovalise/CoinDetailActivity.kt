package com.georgegarside.cryptovalise

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.MenuItem
import com.georgegarside.cryptovalise.presenter.replace
import com.georgegarside.cryptovalise.presenter.rgbToSwatch
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * An activity representing a single Coin detail screen. This activity is only used on narrow width devices.
 * On tablet-size devices, item details are presented side-by-side with a list of items in a [CoinListActivity].
 */
class CoinDetailActivity : AppCompatActivity() {
	companion object {
		const val coinColourKey = "coin_colour"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_coin_detail)
		setSupportActionBar(toolbarDetail)
		
		// Action bar up button to call onBackPressed
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		if (savedInstanceState != null) return
		
		setToolbarColour(intent.getIntExtra(coinColourKey, 0))
		
		val coinSymbol = intent.getStringExtra(CoinDetailFragment.coinSymbolKey)
		
		val fragment = CoinDetailFragment.createFragment(intent.extras)
		
		replace(R.id.coinDetail, fragment)
		
		launch(UI) {
			(chartFragment as? ChartFragment)?.loadChart(coinSymbol)
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		android.R.id.home -> {
			// Up button in top-start of action bar to return to the previous activity
			onBackPressed()
			true
		}
		
		else -> super.onOptionsItemSelected(item)
	}
	
	override fun onAttachFragment(childFragment: Fragment?) {
		super.onAttachFragment(childFragment)
		
		if (childFragment is ChartFragment) {
			childFragment.colour = rgbToSwatch(intent.getIntExtra(coinColourKey, 0))
		}
	}
	
	private fun setToolbarColour(rgb: Int) {
		if (rgb == 0) return
		
		val dominantSwatch = rgbToSwatch(rgb) ?: return
		
		collapsingToolbar?.apply {
			setBackgroundColor(dominantSwatch.rgb)
			setContentScrimColor(dominantSwatch.rgb)
			setExpandedTitleColor(dominantSwatch.bodyTextColor)
			setCollapsedTitleTextColor(dominantSwatch.titleTextColor)
		}
		
		toolbarDetail?.apply {
			//setBackgroundColor(dominantSwatch.rgb)
			//it.setStatusBarScrimColor(dominantSwatch.bodyTextColor)
			navigationIcon?.setColorFilter(dominantSwatch.bodyTextColor, PorterDuff.Mode.SRC_ATOP)
		}
		
	}
}
