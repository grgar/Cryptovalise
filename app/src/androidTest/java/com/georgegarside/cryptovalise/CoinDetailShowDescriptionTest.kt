package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.swipeUp
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinDetailShowDescriptionTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinDetailShowDescriptionTest() {
		Thread.sleep(1200)
		
		onView(withId(R.id.coinRecycler))
				.perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
		
		Thread.sleep(1200)
		
		arrayOf(R.id.prices, R.id.summaryCap, R.id.summaryDom).forEach {
			onView(withId(it))
					.check(matches(isDisplayed()))
					.perform(
							swipeUp(),
							GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)
					)
		}
		
		Thread.sleep(1200)
		
		onView(withId(R.id.coinDescription))
				.check(matches(isDisplayed()))
		
		onView(withId(R.id.coinDescriptionMore))
				.check(matches(isDisplayed()))
				.perform(click())
		
		Thread.sleep(2400)
		
		onView(withId(android.R.id.button3))
				.check(matches(isDisplayed()))
				.perform(click())
	}
}
