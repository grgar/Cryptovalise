package com.georgegarside.cryptovalise

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
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
		
		val floatingActionButton = onView(
				allOf(withId(R.id.fab),
						childAtPosition(
								childAtPosition(
										withId(android.R.id.content),
										0),
								2),
						isDisplayed()))
		floatingActionButton.check(matches(isDisplayed()))
		floatingActionButton.perform(click())
		
		Thread.sleep(1200)
		
/*
		val textView = onView(
				allOf(withId(R.id.alertTitle), withText("Choose a coin"),
						childAtPosition(
								allOf(withId(R.id.title_template),
										childAtPosition(
												withId(R.id.topPanel),
												0)),
								0),
						isDisplayed()))
		textView.check(matches(withText("Choose a coin")))
		
		val button = onView(
				allOf(withId(android.R.id.button2),
						childAtPosition(
								childAtPosition(
										withId(R.id.buttonPanel),
										0),
								0),
						isDisplayed()))
		button.check(matches(isDisplayed()))
		
		val textView2 = onView(
				allOf(withId(android.R.id.text1), withText("XRP Ripple"),
						childAtPosition(
								allOf(withId(R.id.select_dialog_listview),
										childAtPosition(
												withId(R.id.contentPanel),
												0)),
								0),
						isDisplayed()))
		textView2.check(matches(withText("XRP Ripple")))
*/
	}
}
