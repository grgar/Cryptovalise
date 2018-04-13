package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CoinTest {
	
	@Test
	internal fun `logo path`() {
		val coin = Coin(slug = "test")
		assertEquals(API.basePath + "uploads/production/coin/icon/0/test.png", coin.logoPath)
	}
	
	@Test
	internal fun `get id`() {
		val coin = Coin()
		assertEquals(0, coin.id, "default id")
		val coin1 = Coin(1)
		assertEquals(1, coin1.id, "set id")
	}
	
	@Test
	internal fun `get symbol`() {
		val coin1 = Coin(symbol = "abc")
		assertEquals("abc", coin1.symbol, "symbol length 3")
		val coin2 = Coin(symbol = "abcd")
		assertEquals("abcd", coin2.symbol, "symbol length 4")
	}
	
	@Test
	internal fun `get name`() {
		val coin = Coin(name = "test")
		assertEquals("test", coin.name)
	}
	
	@Test
	internal fun `get slug`() {
		val coin = Coin(slug = "test")
		assertEquals("test", coin.slug)
	}
	
	@Test
	internal fun `get full description`() {
		val string = "Lorem ipsum dolor sit amet."
		val coin = Coin(description = string)
		assertEquals(string, coin.description)
	}
	
	@Test
	internal fun `get usd price formatted`() {
		val coin = Coin(price = Coin.Price(150.0, 1.0))
		assertEquals("$ 150.00", coin.price.usdPrice)
	}
	
	@Test
	internal fun `set new price in usd`() {
		val coin = Coin(price = Coin.Price(150.0, 1.0))
		coin.price = Coin.Price(200.0, 1.1)
		assertEquals("$ 200.00", coin.price.usdPrice)
	}
	
	@Test
	internal fun `get price deltas`() {
		val timeDelta = Pair(5.0, 10.0)
		val valDelta = Pair(5.0, 100L)
		val smallValDelta = Pair(5.0, 1)
		val coin = Coin(delta = Coin.Delta(
				hour = timeDelta, day = timeDelta, week = timeDelta,
				cap = valDelta, vol = valDelta, dom = smallValDelta
		))
		assertEquals(timeDelta, coin.delta.hour)
		assertEquals(timeDelta, coin.delta.day)
		assertEquals(timeDelta, coin.delta.week)
		assertEquals(valDelta, coin.delta.cap)
		assertEquals(valDelta, coin.delta.vol)
		assertEquals(smallValDelta, coin.delta.dom)
	}
	
	@Test
	internal fun `set new price deltas`() {
		val timeDelta = Pair(5.0, 10.0)
		val valDelta = Pair(5.0, 100L)
		val smallValDelta = Pair(5.0, 1)
		val coin = Coin(delta = Coin.Delta(
				hour = timeDelta, day = timeDelta, week = timeDelta,
				cap = valDelta, vol = valDelta, dom = smallValDelta
		))
		val timeDelta2 = Pair(6.0, 11.0)
		val valDelta2 = Pair(6.0, 110L)
		val smallValDelta2 = Pair(6.0, 2)
		coin.delta = Coin.Delta(
				hour = timeDelta2, day = timeDelta2, week = timeDelta2,
				cap = valDelta2, vol = valDelta2, dom = smallValDelta2
		)
		assertEquals(timeDelta2, coin.delta.hour)
		assertEquals(timeDelta2, coin.delta.day)
		assertEquals(timeDelta2, coin.delta.week)
		assertEquals(valDelta2, coin.delta.cap)
		assertEquals(valDelta2, coin.delta.vol)
		assertEquals(smallValDelta2, coin.delta.dom)
	}
	
	@Test
	internal fun `get supply`() {
		val coin = Coin(supply = Long.MAX_VALUE)
		assertEquals(Long.MAX_VALUE, coin.supply)
		assertEquals(0L, coin.total)
	}
	
	@Test
	internal fun `get total`() {
		val coin = Coin(total = Long.MAX_VALUE)
		assertEquals(Long.MAX_VALUE, coin.total)
		assertEquals(0L, coin.supply)
	}
	
	@Test
	internal fun `destructuring of coin`() {
		val coin = Coin(
				id = (Math.random() * 100).toInt(),
				symbol = "symbol",
				name = "name",
				slug = "slug",
				description = "description",
				price = Coin.Price(),
				delta = Coin.Delta(),
				supply = (Math.random() * 100).toLong(),
				total = (Math.random() * 100).toLong(),
				links = Coin.Links()
		)
		assertEquals(coin.component1(), coin.id)
		assertEquals(coin.component2(), coin.symbol)
		assertEquals(coin.component3(), coin.name)
		assertEquals(coin.component4(), coin.slug)
		assertEquals(coin.component5(), coin.description)
		assertEquals(coin.component6(), coin.price)
		assertEquals(coin.component7(), coin.delta)
		assertEquals(coin.component8(), coin.supply)
		assertEquals(coin.component9(), coin.total)
		assertEquals(coin.component10(), coin.links)
	}
	
	@Test
	internal fun `copy, equals, hashCode, toString`() {
		val coin = Coin(
				id = (Math.random() * 100).toInt(),
				symbol = "symbol",
				name = "name",
				slug = "slug",
				description = "description",
				price = Coin.Price(),
				delta = Coin.Delta(),
				supply = (Math.random() * 100).toLong(),
				total = (Math.random() * 100).toLong(),
				links = Coin.Links()
		)
		val coinCopy = coin.copy()
		assertTrue(coin == coinCopy, "copy & equals")
		assertTrue(coin.toString() == coinCopy.toString(), "toString")
		assertFalse(coin.toString() == Coin().toString(), "toString empty")
		val coinHash = Coin(
				id = coin.id,
				symbol = "symbol",
				name = "name",
				slug = "slug",
				description = "description",
				price = coin.price,
				delta = coin.delta,
				supply = coin.supply,
				total = coin.total,
				links = coin.links
		)
		assertTrue(coin.hashCode() == coinHash.hashCode(), "hashCode")
	}
}
