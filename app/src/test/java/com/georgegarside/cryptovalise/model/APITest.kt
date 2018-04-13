package com.georgegarside.cryptovalise.model

import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

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
