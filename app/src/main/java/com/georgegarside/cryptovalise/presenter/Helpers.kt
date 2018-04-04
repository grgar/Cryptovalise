package com.georgegarside.cryptovalise.presenter

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

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