package com.georgegarside.cryptovalise.presenter

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.API

/**
 * Helper method to simplify the transaction process of replacing a [fragment] within the specific [containerViewId]
 */
fun FragmentActivity.replace(@IdRes containerViewId: Int, fragment: Fragment) {
	// Replacement is performed on the activity containing a fragment
	this
			// Fragment manager is used replace the fragment within
			.supportFragmentManager
			// Replacing a fragment is part of a transaction
			.beginTransaction()
			// Perform the actual replacement, taking the ID of the container
			// and a reference to the fragment to place within
			.replace(containerViewId, fragment)
			// Perform the transaction
			.commit()
}

/**
 * Extension function to set the colour of a [TextView] containing a delta (that is, a TextView containing text with
 * a unicode up/down triangle as the first character) to either [R.color.deltaUp] or [R.color.deltaDown] based on the
 * [TextView.getText] contained within itself. This method should be run once the TextView text has been set as
 * necessary since it takes no input for the text itself.
 */
fun TextView.setDeltaColour() = when {
	text.startsWith(API.Coin.Delta.upSymbol) ->
		// Positive colour used to indicate a delta increase
		setTextColor(ContextCompat.getColor(context, R.color.deltaUp))
	
	text.startsWith(API.Coin.Delta.downSymbol) ->
		// Negative colour used to indicate a delta decrease
		setTextColor(ContextCompat.getColor(context, R.color.deltaDown))
	
	else ->
		// Neutral colour used for general text, which for a delta signifies neither increase nor decrease
		setTextColor(ContextCompat.getColor(context, R.color.colorAccentText))
}
