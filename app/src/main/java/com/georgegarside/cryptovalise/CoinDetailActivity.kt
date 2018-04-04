package com.georgegarside.cryptovalise

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.MenuItem
import com.georgegarside.cryptovalise.presenter.replace
import kotlinx.android.synthetic.main.activity_coin_detail.*

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
		
		val fragment = CoinDetailFragment.createFragment(Bundle().apply {
			with(CoinDetailFragment.coinSymbolKey) {
				putString(this, intent.getStringExtra(this))
			}
		})
		
		replace(R.id.coinDetail, fragment)
	}
	
	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		android.R.id.home -> {
			// Up button in top-start of action bar to return to the previous activity
			onBackPressed()
			true
		}
		
		else -> super.onOptionsItemSelected(item)
	}
	
	private fun setToolbarColour(rgb: Int) {
		if (rgb == 0) return
		
		val dominantSwatch = Palette.from(listOf(Palette.Swatch(rgb, 1))).dominantSwatch ?: return
		
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
