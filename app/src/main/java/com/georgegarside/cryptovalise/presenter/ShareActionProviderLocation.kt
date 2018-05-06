package com.georgegarside.cryptovalise.presenter

import android.content.Intent
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.view.Menu
import com.georgegarside.cryptovalise.R

/**
 * Defines a container for a share action provider, suitable for holding a future provider and future intent, pairing
 * them together only when both are defined. This supports asynchronous unrestricted loading of each component
 * independently and provides no additional necessary locking.
 */
interface ShareActionProviderLocation {
	/**
	 * The [ShareActionProvider] which provides the ability to share the [shareIntent] through the available providers.
	 */
	var shareActionProvider: ShareActionProvider?
	
	/**
	 * The [Intent] which is to be shared using the [shareActionProvider].
	 */
	var shareIntent: Intent?
	
	/**
	 * Defines a [ShareActionProvider] from a [menu] and applies the stored [shareIntent] to it if defined.
	 */
	fun defineShareActionProvider(menu: Menu) {
		val sap =
				MenuItemCompat.getActionProvider(menu.findItem(R.id.shareMenuItem)) as ShareActionProvider
		shareActionProvider = sap.apply { setShareIntent(shareIntent) }
	}
	
	/**
	 * Defines an [intent] and applies it to the stored [shareActionProvider] if defined.
	 */
	fun defineShareIntent(intent: Intent) {
		shareIntent = intent.also { shareActionProvider?.setShareIntent(intent) }
	}
}
