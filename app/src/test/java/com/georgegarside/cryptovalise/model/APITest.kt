package com.georgegarside.cryptovalise.model

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled

internal class APITest {
	
	@Test
	@Disabled
	internal fun `perform network download`() {
		val bytes = runBlocking {
			API.download("404")
		}
		assertTrue(bytes.isNotEmpty())
	}
}
