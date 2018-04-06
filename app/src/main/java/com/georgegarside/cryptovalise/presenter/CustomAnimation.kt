package com.georgegarside.cryptovalise.presenter

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.TextView
import com.georgegarside.cryptovalise.presenter.CustomAnimation.animateText
import com.georgegarside.cryptovalise.presenter.CustomAnimation.fadeIn
import com.georgegarside.cryptovalise.presenter.CustomAnimation.fadeOut

/**
 * Custom animations used in the app for views, from simple [AlphaAnimation] of [fadeIn] or [fadeOut], to providing
 * support for extension functions by being able to [animateText] for text transition in views and associated headers.
 */
object CustomAnimation {
	/**
	 * A basic fade in [AlphaAnimation]
	 */
	val fadeIn = AlphaAnimation(0f, 1f)
	
	/**
	 * A basic fade out [AlphaAnimation]
	 */
	internal val fadeOut = AlphaAnimation(1f, 0f)
	
	init {
		arrayOf(fadeIn, fadeOut).forEach {
			it.duration = 500
			it.fillAfter = true
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
			alpha = 1f
			this.animation = animation
		}
		additionalView?.apply {
			alpha = 1f
			this.animation = animation
		}
	}
}

/**
 * [TextView.setText] of the [TextView] to the given [text] and [fadeIn] this text when changed.
 * If an [additionalView] is given, [fadeIn] this [View] at the same time.
 */
fun TextView.fadeInText(text: String, additionalView: View? = null) =
		CustomAnimation.animateText(this, CustomAnimation.fadeIn, text, additionalView)

/**
 * Increase (or decrease) the [ProgressBar] progress by the [delta] given, with an [ObjectAnimator] animation.
 * If the progress bar is completed (the [ProgressBar.getProgress] has reached [ProgressBar.getMax]) once the delta has
 * been applied, the progress bar will [CustomAnimation.fadeOut].
 *
 * Reverse progress is supported through negative [delta] input. Once the progress bar reaches [ProgressBar.getMax],
 * the progress bar is hidden. A previous revision of this function would show the progress bar again if necessary,
 * however this causes unwanted flashing in cases where the progress bar is reloaded at completion, so this was deemed
 * unnecessary.
 */
fun ProgressBar.progressAnimate(delta: Int) {
	// Calculate new percentage
	val newProgress = progress + delta
	
	// Animate progress bar up to given progress
	val animator = ObjectAnimator.ofInt(this, "progress", newProgress).apply {
		duration = 800
		start()
	}
	
	// Fade out progress bar if completed
	if (newProgress >= this.max) {
		this.animation = fadeOut
	}
}

infix fun <T : View> View.now(show: T): T {
	apply {
		alpha = 0f
		animation = fadeOut
	}
	return show.apply {
		alpha = 1f
		animation = fadeIn
	}
}
