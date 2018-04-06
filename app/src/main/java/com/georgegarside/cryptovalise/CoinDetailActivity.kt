package com.georgegarside.cryptovalise

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.MenuItem
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
		
		// If an instance was saved previously, there is no need to recreate any content here
		if (savedInstanceState != null) return
		
		// Set the colour as soon as possible as this defines the ‘theme’ of this activity
		setToolbarColour(intent.getIntExtra(coinColourKey, 0))
	}
	
	/**
	 * Set the up button to return to previous activity using standard back action rather than recreating the activity.
	 */
	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		android.R.id.home -> {
			// Up button in top-start of action bar to return to the previous activity
			onBackPressed()
			true
		}
		
		else -> super.onOptionsItemSelected(item)
	}
	
	/**
	 * Perform extra handling of fragments within this activity: [ChartFragment].
	 */
	override fun onAttachFragment(childFragment: Fragment?) {
		super.onAttachFragment(childFragment)
		
		// Pass all the intent extras received by this activity onwards
		childFragment?.arguments = intent.extras
	}
	
	/**
	 * Set the colour of the [toolbarDetail] and [collapsingToolbar] based on the given [rgb]. The given colour is only
	 * applied literally to the background, and then derived colours are set to other elements using [Palette].
	 */
	private fun setToolbarColour(rgb: Int) {
		// Ignore no colour
		if (rgb == 0) return
		
		// Convert the given RGB to a swatch for calculating associated colours based on this
		val dominantSwatch = rgbToSwatch(rgb) ?: return
		
		// Apply colours to extended height toolbar containing activity title and chart
		collapsingToolbar?.apply {
			setBackgroundColor(dominantSwatch.rgb)
			setContentScrimColor(dominantSwatch.rgb)
			setExpandedTitleColor(dominantSwatch.bodyTextColor)
			setCollapsedTitleTextColor(dominantSwatch.titleTextColor)
		}
		
		// Apply colours to app bar
		toolbarDetail?.apply {
			// TODO: Determine way to set colour of status bar
			//setBackgroundColor(dominantSwatch.rgb)
			//it.setStatusBarScrimColor(dominantSwatch.bodyTextColor)
			navigationIcon?.setColorFilter(dominantSwatch.bodyTextColor, PorterDuff.Mode.SRC_ATOP)
		}
	}
}
