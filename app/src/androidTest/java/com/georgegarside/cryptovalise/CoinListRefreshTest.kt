package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinListRefreshTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinListRefreshTest() {
		Thread.sleep(1200)
		
		val symbolTextView = onView(allOf(
				withId(R.id.symbol),
				withText("BTC"),
				isDisplayed()
		))
		symbolTextView.check(matches(withText("BTC")))
		
		val titleTextView = onView(allOf(
				withId(R.id.coinName),
				withText("Bitcoin"),
				isDisplayed()
		))
		titleTextView.check(matches(withText("Bitcoin")))
		
		onView(withId(R.id.coinRecycler))
				.perform(
						GeneralSwipeAction(Swipe.SLOW, GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER, Press.FINGER)
				)
		
		Thread.sleep(1200)
		
		symbolTextView.check(matches(withText("BTC")))
		titleTextView.check(matches(withText("Bitcoin")))
	}
}
