package com.georgegarside.cryptovalise.model

/**
 * A currency from the [API]. Each currency has a currency [code] which is the standard code used to represent the
 * currency internationally (this is unique for each currency), a [name] in English for user-identification of the
 * currency, and the exchange [rate] of the currency with USD.
 */
data class Currency(
		/**
		 * The international standard currency code for this currency.
		 */
		val code: String = "",
		/**
		 * The English name for the currency.
		 */
		val name: String = "",
		/**
		 * The exchange rate of the currency with USD.
		 */
		val rate: Double = 0.0
)
