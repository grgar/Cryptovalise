package com.georgegarside.cryptovalise.presenter

import android.graphics.PorterDuff
import android.os.Build
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.Coin
import kotlinx.android.synthetic.main.activity_coin_detail.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Extension function to set the colour of a [TextView] containing a delta (that is, a TextView containing text with
 * a unicode up/down triangle as the first character) to either [R.color.deltaUp] or [R.color.deltaDown] based on the
 * [TextView.getText] contained within itself. This method should be run once the TextView text has been set as
 * necessary since it takes no input for the text itself.
 */
fun TextView.setDeltaColour() = when {
	text.startsWith(Coin.Delta.upSymbol) ->
		// Positive colour used to indicate a delta increase
		setTextColor(ContextCompat.getColor(context, R.color.deltaUp))
	
	text.startsWith(Coin.Delta.downSymbol) ->
		// Negative colour used to indicate a delta decrease
		setTextColor(ContextCompat.getColor(context, R.color.deltaDown))
	
	else ->
		// Neutral colour used for general text, which for a delta signifies neither increase nor decrease
		setTextColor(ContextCompat.getColor(context, R.color.colorAccentText))
}

/**
 * Create a [Palette] swatch from a colour's RGB [Int] value. This allows other colours to be derived from it, such as a
 * compatible text colour to be placed on this colour's background.
 */
fun Int.toSwatch() = Palette.from(listOf(Palette.Swatch(this, 1))).dominantSwatch

/**
 * Set the colour of the [toolbarDetail] and [collapsingToolbar] based on the given [rgb]. The given colour is only
 * applied literally to the background, and then derived colours are set to other elements using [Palette].
 */
fun CollapsingToolbarLayout.setColour(rgb: Int, window: Window, toolbar: Toolbar) {
	// Convert the given RGB to a swatch for calculating associated colours based on this
	val dominantSwatch = rgb.toSwatch() ?: return
	
	// Apply colours to extended height toolbar containing activity title and chart
	setBackgroundColor(dominantSwatch.rgb)
	setContentScrimColor(dominantSwatch.rgb)
	setExpandedTitleColor(dominantSwatch.bodyTextColor)
	setCollapsedTitleTextColor(dominantSwatch.titleTextColor)
	
	// Apply colours to app bar
	toolbar.setColour(rgb, true)
	
	// Apply colours to status bar
	if (!window.setStatusBarColour(rgb)) this.setStatusBarScrimColor(dominantSwatch.titleTextColor)
}

/**
 * Set the background colour of the toolbar at the top of the device. This also appropriately colours the contents of
 * the toolbar so the contents is still legible against the background colour selected. The [rgb] is applied to the
 * background, then the title and subtitle are set to a [Palette]-derived colour to be legible. The menu icons are
 * coloured to be legible against the background using [PorterDuff.Mode.SRC_ATOP]. If the toolbar
 * [isInCollapsingToolbarLayout] the layout is responsible for setting the colour itself, and this function does not
 * apply any colour changes which would affect the background scrim of that view when collapsed.
 */
fun Toolbar.setColour(rgb: Int, isInCollapsingToolbarLayout: Boolean = false) {
	val dominantSwatch = rgb.toSwatch() ?: return
	
	if (!isInCollapsingToolbarLayout) setBackgroundColor(dominantSwatch.rgb)
	setTitleTextColor(dominantSwatch.titleTextColor)
	setSubtitleTextColor(dominantSwatch.bodyTextColor)
	
	navigationIcon?.setColorFilter(dominantSwatch.bodyTextColor, PorterDuff.Mode.SRC_ATOP)
	menu.iterator().forEach {
		it.icon.apply {
			mutate()
			setColorFilter(dominantSwatch.bodyTextColor, PorterDuff.Mode.SRC_ATOP)
		}
	}
}

/**
 * Set the background colour of the status bar. This is only available on [Build.VERSION_CODES.LOLLIPOP] or later and
 * returns a boolean indicating whether the operation was successful. If this returns false, it is likely that the
 * caller needs to perform additional steps to colour the status bar, such as using a support scrim on a collapsing
 * layout, which will be under the status bar on older Android versions.
 */
fun Window.setStatusBarColour(rgb: Int): Boolean =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			statusBarColor = rgb.toSwatch()?.titleTextColor ?: statusBarColor
			navigationBarColor = rgb.toSwatch()?.rgb ?: navigationBarColor
			true
		} else false

/**
 * Make the [Menu] conform to [Iterator], such that the items within can be iterated through with a forEach.
 */
fun Menu.iterator() = object : Iterator<MenuItem> {
	var currentIndex = 0
	override fun hasNext(): Boolean = currentIndex < size()
	override fun next(): MenuItem = getItem(currentIndex++)
}

/**
 * Check for network connectivity and show a [Snackbar] for the [view] with a refresh button to enact a [callback].
 */
suspend fun checkNetwork(view: View, callback: suspend () -> Unit = {}) = async {
	try {
		// Basic network check, DNS for CloudFlare
		Socket().apply {
			connect(InetSocketAddress("1.1.1.1", 53), 1500)
			close()
		}
	} catch (e: IOException) {
		// No internet connectivity
		Snackbar.make(view, view.context.getString(R.string.error_no_network), Snackbar.LENGTH_INDEFINITE).apply {
			setAction(view.context.getString(R.string.error_no_network_action), {
				dismiss()
				launch(UI) { callback() }
			})
			show()
		}
		return@async false
	}
	return@async true
}.await()
