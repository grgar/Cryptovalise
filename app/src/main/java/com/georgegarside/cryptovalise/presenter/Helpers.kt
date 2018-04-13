package com.georgegarside.cryptovalise.presenter

import android.graphics.PorterDuff
import android.os.Build
import android.support.annotation.IdRes
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import com.georgegarside.cryptovalise.R
import com.georgegarside.cryptovalise.model.Coin
import kotlinx.android.synthetic.main.activity_coin_detail.*

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

fun Window.setStatusBarColour(rgb: Int): Boolean =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			statusBarColor = rgb.toSwatch()?.titleTextColor ?: statusBarColor
			navigationBarColor = rgb.toSwatch()?.rgb ?: navigationBarColor
			true
		} else false

fun Menu.iterator() = object : Iterator<MenuItem> {
	var currentIndex = 0
	override fun hasNext(): Boolean = currentIndex < size()
	override fun next(): MenuItem = getItem(currentIndex++)
}
