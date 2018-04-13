package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
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
class ChartFragmentTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun chartFragmentTest() {
		Thread.sleep(1200)
		
		onView(withId(R.id.coinRecycler)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
		
		Thread.sleep(2400)
		
		val chartFragment = onView(withId(R.id.chartFragment))
				.check(matches(isDisplayed()))
				.perform(click())
		
		Thread.sleep(1200)
		
		chartFragment
				.perform(click())
				.check(matches(isDisplayed()))
	}
}
