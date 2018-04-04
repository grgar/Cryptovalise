package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.georgegarside.cryptovalise.presenter.replace
import kotlinx.android.synthetic.main.activity_coin_detail.*

/**
 * An activity representing a single Coin detail screen. This activity is only used on narrow width devices.
 * On tablet-size devices, item details are presented side-by-side with a list of items in a [CoinListActivity].
 */
class CoinDetailActivity : AppCompatActivity() {
	
	companion object {
		fun createFragment(bundle: Bundle) =
				CoinDetailFragment().apply {
					arguments = bundle
				}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_coin_detail)
		setSupportActionBar(toolbarDetail)
		
		// Action bar up button to call onBackPressed
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		if (savedInstanceState == null) {
			val fragment = createFragment(Bundle().apply {
				with(CoinDetailFragment.coinSymbolKey) {
					putString(this, intent.getStringExtra(this))
				}
			})
			
			replace(R.id.coinDetail, fragment)
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
	// Up button in top-start of action bar to return to the previous activity
		android.R.id.home -> {
			onBackPressed()
			true
		}
		
		else -> super.onOptionsItemSelected(item)
	}
}
