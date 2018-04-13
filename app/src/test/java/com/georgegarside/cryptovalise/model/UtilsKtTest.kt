package com.georgegarside.cryptovalise.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class UtilsKtTest {
	
	@ParameterizedTest(name = "run #{index} with [{arguments}]")
	@ValueSource(doubles = [0.0, 1.1, 3.2, 9.9, 10.0, 99.9, 100.0])
	internal fun `format extension fun for double`(double: Double) =
			assertEquals(NumberFormat.format(double), double.format())
	
	@ParameterizedTest(name = "run #{index} with [{arguments}]")
	@ValueSource(doubles = [0.0, 1.1, 3.2, 9.9, 10.0, 99.9, 100.0])
	internal fun `format extension fun for double with specified format`(double: Double) {
		assertEquals(NumberFormat.Small.format(double), double.format(NumberFormat.Small))
		assertEquals(NumberFormat.Normal.format(double), double.format(NumberFormat.Normal))
		assertEquals(NumberFormat.Large.format(double), double.format(NumberFormat.Large))
		assertEquals(NumberFormat.Delta.format(double), double.format(NumberFormat.Delta))
	}
	
	@ParameterizedTest(name = "run #{index} with [{arguments}]")
	@ValueSource(doubles = [0.0, 1.1, 3.2, 9.9, 10.0, 99.9, 100.0])
	internal fun `format extension fun for double with specified format and suffix`(double: Double) {
		assertEquals(NumberFormat.Small.format(double, "suffix"), double.format(NumberFormat.Small, "suffix"))
		assertEquals(NumberFormat.Normal.format(double, "suffix"), double.format(NumberFormat.Normal, "suffix"))
		assertEquals(NumberFormat.Large.format(double, "suffix"), double.format(NumberFormat.Large, "suffix"))
		assertEquals(NumberFormat.Delta.format(double, "suffix"), double.format(NumberFormat.Delta, "suffix"))
	}
	
	@ParameterizedTest(name = "run #{index} with [{arguments}]")
	@ValueSource(longs = [0L, 1L, 9L, 10L, 99L, 100L])
	internal fun `format extension fun for long`(long: Long) =
			assertEquals(NumberFormat.format(long), long.format())
}
