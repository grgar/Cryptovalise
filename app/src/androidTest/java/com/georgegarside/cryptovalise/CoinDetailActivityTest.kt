package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinDetailActivityTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinDetailActivityTest() {
		Thread.sleep(1200)
		
		onView(withId(R.id.coinRecycler)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
		
		Thread.sleep(1200)
		
		onView(withId(R.id.prices)).check(matches(isDisplayed()))
		onView(withId(R.id.summaryCap)).check(matches(isDisplayed()))
		onView(withId(R.id.summaryVol)).check(matches(isDisplayed()))
		onView(withId(R.id.summaryDom)).check(matches(isDisplayed()))
		
		onView(allOf(
				withContentDescription("Navigate up"),
				isDisplayed()
		)).perform(click())
		
		Thread.sleep(1200)
		
		onView(withId(R.id.coinRecycler)).check(matches(isDisplayed()))
	}
}
