package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CurrencyTest {
	
	@Test
	internal fun `get symbol`() {
		val currency = Currency(code = "abc")
		assertEquals("abc", currency.code)
	}
	
	@Test
	internal fun `get name`() {
		val currency = Currency(name = "test")
		assertEquals("test", currency.name)
	}
	
	@Test
	internal fun `get rate`() {
		val currency = Currency(rate = 3.2)
		assertEquals(3.2, currency.rate)
	}
	
	@Test
	internal fun `destructuring of currency`() {
		val currency = Currency(
				code = "code",
				name = "name",
				rate = 3.2
		)
		assertEquals(currency.component1(), currency.code)
		assertEquals(currency.component2(), currency.name)
		assertEquals(currency.component3(), currency.rate)
	}
	
	@Test
	internal fun `copy, equals, hashCode & toString`() {
		val currency = Currency(
				code = "code",
				name = "name",
				rate = 3.2
		)
		val currencyCopy = currency.copy()
		assertTrue(currency == currencyCopy, "copy & equals")
		assertTrue(currency.toString() == currencyCopy.toString(), "toString")
		assertFalse(currency.toString() == Currency().toString(), "toString empty")
		val currencyHash = Currency(
				code = "code",
				name = "name",
				rate = 3.2
		)
		assertTrue(currency.hashCode() == currencyHash.hashCode(), "hashCode")
	}
}
