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
class CoinListDeleteCoinTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinListDeleteCoinTest() {
		Thread.sleep(1200)
		
		val symbolTextView = onView(allOf(
				withId(R.id.symbol),
				withText("ETH"),
				isDisplayed()
		))
		symbolTextView.check(matches(withText("ETH")))
		
		val titleTextView = onView(allOf(
				withId(R.id.coinName),
				withText("Ethereum"),
				isDisplayed()
		))
		titleTextView.check(matches(withText("Ethereum")))
		
		val moreButton = onView(allOf(
				withId(R.id.buttonMore),
				withContentDescription("More"),
				isDisplayed()
		))
		moreButton.check(matches(isDisplayed()))
		moreButton.perform(click())
		
		val deleteMenuItem = onView(allOf(
				withId(R.id.title),
				withText("Delete"),
				isDisplayed()
		))
		deleteMenuItem.perform(click())
		
		symbolTextView.check(doesNotExist())
		titleTextView.check(doesNotExist())
		deleteMenuItem.check(doesNotExist())
	}
}
