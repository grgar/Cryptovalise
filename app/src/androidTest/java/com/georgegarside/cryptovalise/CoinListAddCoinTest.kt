package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinListAddCoinTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinListAddCoinTest() {
		Thread.sleep(1200)
		
		onView(allOf(withId(R.id.fab), isDisplayed()))
				.check(matches(isDisplayed()))
				.perform(click())
		
		Thread.sleep(1200)
		
		val titleTextView = onView(allOf(
				withId(R.id.alertTitle),
				withText(R.string.add_coin_title),
				isDisplayed()
		))
		titleTextView.check(matches(withText(R.string.add_coin_title)))
		
		val cancelButton = onView(
				withId(android.R.id.button2)
		)
		cancelButton.check(matches(isDisplayed()))
		
		val rowText = onView(allOf(
				withId(android.R.id.text1),
				withText("XRP Ripple"),
				isDisplayed()
		))
		rowText.check(matches(withText("XRP Ripple")))
		
		cancelButton.perform(click())
		
		titleTextView.check(doesNotExist())
	}
}
