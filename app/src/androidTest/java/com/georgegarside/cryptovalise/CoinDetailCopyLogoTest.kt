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
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinDetailCopyLogoTest {
	
	@Suppress("unused")
	@get:Rule
	var activityTestRule = ActivityTestRule(CoinListActivity::class.java)
	
	@Test
	fun coinDetailCopyLogoTest() {
		Thread.sleep(1200)
		
		onView(withId(R.id.coinRecycler))
				.perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
		
		Thread.sleep(1200)
		
		onView(withId(R.id.prices))
				.check(matches(isDisplayed()))
				.perform(
						swipeUp(),
						GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)
				)
		onView(withId(R.id.summaryCap))
				.check(matches(isDisplayed()))
				.perform(
						swipeUp(),
						GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)
				)
		onView(withId(R.id.summaryDom))
				.check(matches(isDisplayed()))
				.perform(
						swipeUp(),
						GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)
				)
		
		Thread.sleep(1200)
		
		onView(withId(R.id.coinLogoCopy))
				.check(matches(isDisplayed()))
				.perform(click())
		
		Thread.sleep(1200)
		
		onView(allOf(
				withId(android.support.design.R.id.snackbar_text),
				withText(R.string.coin_detail_copy_logo_done)
		))
				.check(matches(isDisplayed()))
	}
}
