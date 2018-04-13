package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinListActivityTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinListActivityTest() {
		Thread.sleep(1200)
		
		val textView = onView(
				allOf(withId(R.id.symbol), withText("BTC"),
						childAtPosition(
								childAtPosition(
										IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
										0),
								1),
						isDisplayed()))
		textView.check(matches(withText("BTC")))
		
		val textView2 = onView(
				allOf(withId(R.id.coinName), withText("Bitcoin"),
						childAtPosition(
								childAtPosition(
										IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
										0),
								2),
						isDisplayed()))
		textView2.check(matches(withText("Bitcoin")))
	}
}
