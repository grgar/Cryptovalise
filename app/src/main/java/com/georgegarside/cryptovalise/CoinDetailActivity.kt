package com.georgegarside.cryptovalise

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.view.Menu
import android.view.MenuItem
import com.georgegarside.cryptovalise.presenter.ShareActionProviderLocation
import com.georgegarside.cryptovalise.presenter.setColour
import kotlinx.android.synthetic.main.activity_coin_detail.*

/**
 * An activity representing a single Coin detail screen. This activity is only used on narrow width devices.
 * On tablet-size devices, item details are presented side-by-side with a list of items in a [CoinListActivity] and this
 * activity is unused. See the report for a diagram of how the activities and fragments are connected.
 */
class CoinDetailActivity(
		
		// Implements ShareActionProviderLocation custom interface
		override var shareActionProvider: ShareActionProvider? = null,
		override var shareIntent: Intent? = null

) : AppCompatActivity(), ShareActionProviderLocation {
	
	companion object {
		const val coinColourKey = "coin_colour"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_coin_detail)
		setSupportActionBar(toolbarDetail)
		
		// Wait for async before transition
		supportPostponeEnterTransition()
		
		appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
			chartFragment.view?.alpha = 1 - Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
		}
		
		// Action bar up button to call onBackPressed
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		// Set the colour as soon as possible as this defines the ‘theme’ of this activity
		intent.getIntExtra(coinColourKey, 0).let {
			collapsingToolbar.setColour(
					if (it != 0) it else ContextCompat.getColor(this, R.color.colorPrimary),
					window, toolbarDetail
			)
		}
	}
	
	/**
	 * Inflate a [menu] into the toolbar, and set click listener for menu items
	 */
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.coindetail, menu)
		defineShareActionProvider(menu)
		return true
	}
	
	/**
	 * Set the up button to return to previous activity using standard back action rather than recreating the activity.
	 */
	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		android.R.id.home -> {
			// Up button in top-start of action bar to return to the previous activity
			supportPostponeEnterTransition()
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
}
