package com.georgegarside.cryptovalise.model

import com.georgegarside.cryptovalise.presenter.format
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HelpersKtTest {
	
	@Test
	fun formatNoChange() {
		assertEquals("1", 1L.format())
		assertEquals("12", 12L.format())
		assertEquals("123", 123L.format())
	}
	
	@Test
	fun formatK() {
		assertEquals("1k", 1234L.format())
		assertEquals("12k", 12345L.format())
		assertEquals("123k", 123456L.format())
	}
	
	@Test
	fun formatM() {
		assertEquals("1M", 1234567L.format())
		assertEquals("12M", 12345678L.format())
		assertEquals("123M", 123456789L.format())
	}
}
