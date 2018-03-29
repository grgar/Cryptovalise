package com.georgegarside.cryptovalise

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.georgegarside.cryptovalise.model.replace
import kotlinx.android.synthetic.main.activity_currency_detail.*

/**
 * An activity representing a single Coin detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [CurrencyListActivity].
 */
class CurrencyDetailActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_currency_detail)
		setSupportActionBar(detail_toolbar)
		
		// Show the Up button in the action bar.
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			val fragment = CurrencyDetailFragment().apply {
				arguments = Bundle().apply {
					putString(CurrencyDetailFragment.ARG_ITEM_ID,
							intent.getStringExtra(CurrencyDetailFragment.ARG_ITEM_ID))
				}
			}
			
			replace(R.id.currencyDetail, fragment)
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem) =
			when (item.itemId) {
				android.R.id.home -> {
					// This ID represents the Home or Up button. In the case of this
					// activity, the Up button is shown. For
					// more details, see the Navigation pattern on Android Design:
					//
					// http://developer.android.com/design/patterns/navigation.html#up-vs-back
					
					navigateUpTo(Intent(this, CurrencyListActivity::class.java))
					true
				}
				else -> super.onOptionsItemSelected(item)
			}
}
