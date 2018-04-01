package com.georgegarside.cryptovalise.presenter

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Custom animations used in the app for views, from simple [AlphaAnimation] of [fadeIn] or [fadeOut], to providing
 * support for extension functions by being able to [animateText] for text transition in views and associated headers.
 */
object CustomAnimation {
	/**
	 * A basic fade in [AlphaAnimation] lasting 0.3s, with the object visible once complete
	 */
	val fadeIn = AlphaAnimation(0.0f, 1.0f)
	
	/**
	 * A basic fade out [AlphaAnimation] lasting 0.5s, with the object invisible once complete
	 */
	private val fadeOut = AlphaAnimation(1.0f, 0.0f)
	
	init {
		fadeIn.apply {
			duration = 300
			fillAfter = true
		}
		fadeOut.apply {
			duration = 500
			fillAfter = true
		}
	}
	
	/**
	 * Animate a [textView] from current text to given [string] using an [animation].
	 * Also applies the [animation] to the [additionalView] if given, such as a header for the [textView] to animate at
	 * the same time.
	 */
	internal fun animateText(textView: TextView, animation: Animation, string: String, additionalView: View?) {
		textView.apply {
			text = string
			alpha = 1.0f
			this.animation = animation
		}
		additionalView?.apply {
			alpha = 1.0f
			this.animation = animation
		}
	}
}

fun TextView.fadeInText(text: String, additionalView: View? = null) =
		CustomAnimation.animateText(this, CustomAnimation.fadeIn, text, additionalView)

/**
 * Increase (or decrease) the [ProgressBar] progress by the [delta] given, with an [ObjectAnimator] animation.
 * If the progress bar is completed once the delta has been applied, the progress bar will [CustomAnimation.fadeOut].
 *
 * Reverse progress is supported through negative [delta] input. Once the progress bar reaches 100, the progress bar is
 * hidden. A previous revision of this function would show the progress bar again if necessary, however this causes
 * unwanted flashing in cases where the progress bar is reloaded at full completion, so this was deemed unnecessary.
 */
fun ProgressBar.progressAnimate(delta: Int) {
	// Calculate new percentage
	val newProgress = progress + delta
	
	// Animate progress bar up to given progress
	val animator = ObjectAnimator.ofInt(this, "progress", newProgress).apply {
		duration = 600
		start()
	}
	
	// Fade out progress bar if completed
	if (newProgress >= this.max) {
		ObjectAnimator.ofFloat(this@progressAnimate, "alpha", 0.0f).setDuration(500).start()
	}
}
