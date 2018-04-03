package com.georgegarside.cryptovalise.model

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.google.gson.internal.LinkedTreeMap
import java.util.*

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
 * A [LinkedTreeMap], from [String] to [ArrayList] of [LinkedTreeMap], from [String] to [Any]
 */
typealias ArrayListInMap = LinkedTreeMap<String, ArrayList<LinkedTreeMap<String, Any>>>

val intSuffix: NavigableMap<Long, String> = TreeMap<Long, String>().apply {
	put(1_000, "k")
	put(1_000_000, "M")
	put(1_000_000_000, "G")
	put(1_000_000_000_000, "T")
	put(1_000_000_000_000_000, "P")
	put(1_000_000_000_000_000_000, "E")
}

fun Long.toFormatString(): String {
	if (this < 0) {
		return "–" + this.unaryMinus().toFormatString()
	}
	if (this < 1_000) {
		return this.toString()
	}
	
	val (key, value) = intSuffix.floorEntry(this)
	return (this / key).toString() + value
}
