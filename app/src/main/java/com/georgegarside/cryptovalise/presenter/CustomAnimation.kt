package com.georgegarside.cryptovalise.presenter

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.TextView

object CustomAnimation {
	val fadeIn = AlphaAnimation(0.0f, 1.0f)
	val fadeOut = AlphaAnimation(1.0f, 0.0f)
	
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
	
	internal fun animateText(textView: TextView, anim: Animation, string: String, additionalView: View?) {
		textView.apply {
			text = string
			alpha = 1.0f
			animation = anim
		}
		additionalView?.apply {
			alpha = 1.0f
			animation = anim
		}
	}
}

fun TextView.fadeInText(text: String, additionalView: View? = null) =
		CustomAnimation.animateText(this, CustomAnimation.fadeIn, text, additionalView)

fun ProgressBar.progressAnimate(delta: Int) {
	// Ensure progress bar is visible if progress happens to be reversed off 100% completion
	alpha = 1.0f
	
	// Calculate new percentage
	val newProgress = progress + delta
	
	// Animate progress bar up to given progress
	val animator = ObjectAnimator.ofInt(this, "progress", newProgress).apply {
		duration = 300
		start()
	}
	
	// Fade out progress bar if completed
	if (newProgress >= this.max) {
		ObjectAnimator.ofFloat(this@progressAnimate, "alpha", 0.0f).setDuration(500).start()
	}
}
