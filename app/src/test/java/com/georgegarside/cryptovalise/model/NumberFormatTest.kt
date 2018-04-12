package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Tests for the [NumberFormat] enum.
 */
internal class NumberFormatTest {
	
	/**
	 * Longs can have a suffix appended to the formatted number if they are large enough.
	 */
	@Test
	fun `format longs with suffix`() {
		assertAll("format longs without suffix",
				{ assertEquals("1", 1L.format()) },
				{ assertEquals("12", 12L.format()) },
				{ assertEquals("123", 123L.format()) }
		)
		assertAll("format longs with k suffix",
				{ assertEquals("1k", 1234L.format()) },
				{ assertEquals("12k", 12345L.format()) },
				{ assertEquals("123k", 123456L.format()) }
		)
		assertAll("format longs with M suffix",
				{ assertEquals("1M", 1234567L.format()) },
				{ assertEquals("12M", 12345678L.format()) },
				{ assertEquals("123M", 123456789L.format()) }
		)
	}
	
	/**
	 * Doubles less than 10 are formatted with a fixed 4 decimal places.
	 */
	@Test
	fun `format small doubles to 4dp`() {
		assertEquals("0.0000", 0.0.format())
		assertEquals("1.0000", 1.0.format())
		assertEquals("9.9999", 9.9999.format())
	}
	
	/**
	 * Doubles greater than or equal to 10 are formatted with a fixed 2 decimal places.
	 */
	@Test
	fun `format normal doubles to 2dp`() {
		assertEquals("10.00", 10.0.format())
		assertEquals("99.99", 99.99.format())
		assertEquals("100.00", 100.0.format())
		assertEquals("999.99", 999.99.format())
	}
	
	/**
	 * Separators should be inserted into the formatted string for numbers greater than or equal to 1,000.
	 */
	@Test
	fun `format doubles with separators`() {
		assertEquals("1,000.00", 1_000.0.format())
		assertEquals("9,999.99", 9_999.99.format())
	}
	
	/**
	 * Rounding is supported.
	 */
	@Test
	fun `format doubles by rounding`() {
		assertEquals("100.00", 99.999.format())
		assertEquals("1,000.00", 999.999.format())
		assertEquals("10,000.00", 9_999.999.format())
	}
	
	@Test
	fun `format doubles to specific format`() {
		assertEquals("100.0000", 100.0.format(NumberFormat.Small))
		assertEquals("1.00", 1.0.format(NumberFormat.Normal))
	}
	
	@Test
	fun maxLong() {
		val manualFormat = Long.MAX_VALUE.toString().foldIndexed("", { index, c, acc ->
			if (index > 0 && index % 3 == 0) "$acc,$c" else "$acc$c"
		})
		
		assertNotEquals(manualFormat, Long.MAX_VALUE.format())
	}
}
