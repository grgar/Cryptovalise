package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NumberFormatTest {
	
	@Test
	fun formatLongs() {
		assertEquals("1", 1L.format())
		assertEquals("12", 12L.format())
		assertEquals("123", 123L.format())
		assertEquals("1k", 1234L.format())
		assertEquals("12k", 12345L.format())
		assertEquals("123k", 123456L.format())
		assertEquals("1M", 1234567L.format())
		assertEquals("12M", 12345678L.format())
		assertEquals("123M", 123456789L.format())
	}
	
	/**
	 * Doubles less than 10 are formatted with a fixed 4 decimal places.
	 */
	@Test
	fun formatDoubles4dp() {
		assertEquals("0.0000", 0.0.format())
		assertEquals("1.0000", 1.0.format())
		assertEquals("9.9999", 9.9999.format())
	}
	
	/**
	 * Doubles greater than or equal to 10 are formatted with a fixed 2 decimal places.
	 */
	@Test
	fun formatDoubles2dp() {
		assertEquals("10.00", 10.0.format())
		assertEquals("99.99", 99.99.format())
		assertEquals("100.00", 100.0.format())
		assertEquals("999.99", 999.99.format())
	}
	
	/**
	 * Separators should be inserted into the formatted string for numbers greater than or equal to 1,000.
	 */
	@Test
	fun formatDoublesSeparators() {
		assertEquals("1,000.00", 1_000.0.format())
		assertEquals("9,999.99", 9_999.99.format())
	}
	
	/**
	 * Rounding is supported.
	 */
	@Test
	fun formatDoublesRounding() {
		assertEquals("100.00", 99.999.format())
		assertEquals("1,000.00", 999.999.format())
		assertEquals("10,000.00", 9_999.999.format())
		
	}
}
