package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HelpersKtTest {
	
	@Test
	fun toFormatStringNoChange() {
		assertEquals("1", 1L.toFormatString())
		assertEquals("12", 12L.toFormatString())
		assertEquals("123", 123L.toFormatString())
	}
	
	@Test
	fun toFormatStringK() {
		assertEquals("1k", 1234L.toFormatString())
		assertEquals("12k", 12345L.toFormatString())
		assertEquals("123k", 123456L.toFormatString())
	}
	
	@Test
	fun toFormatStringM() {
		assertEquals("1M", 1234567L.toFormatString())
		assertEquals("12M", 12345678L.toFormatString())
		assertEquals("123M", 123456789L.toFormatString())
	}
}
