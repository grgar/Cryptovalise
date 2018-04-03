package com.georgegarside.cryptovalise.presenter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Number formatter for formatting [Double] or [Long] to be displayed in UI. This is a custom formatter object which
 * utilises [DecimalFormat] to provide standardised formatting of numbers.
 *
 * NumberFormat supports formatting different sizes of numbers in different ways. See the enum [Format] for specific
 * descriptions of the formatting styles available.
 */
object NumberFormat {
	
	/**
	 * The formatters used by this object to perform formatting of numbers. The given pattern for an enum determines how
	 * those numbers passed to the [Format.format] function will be formatted and returned as a string.
	 */
	private enum class Format(pattern: String) {
		/**
		 * Small numbers, less than 10, which are formatted with precisely 4 decimal places.
		 */
		Small("0.0000"),
		/**
		 * General purpose formatting with precisely 2 decimal places.
		 */
		Normal("#,##0.00"),
		/**
		 * Integer only, supports up to [Long.MAX_VALUE].
		 */
		Large("#,###,###,###,###,###,##0");
		
		/**
		 * An instance of a [DecimalFormat] with the [symbols] set as defined in [NumberFormat].
		 */
		private val decimalFormat = DecimalFormat(pattern).apply {
			decimalFormatSymbols = symbols
		}
		
		/**
		 * Perform the formatting of a [number] using the chosen [Format] from the enum.
		 * Returns a string formatted representation of the number given.
		 */
		fun format(number: Number): String = decimalFormat.format(number)
	}
	
	/**
	 * The [DecimalFormatSymbols] used for formatting numbers with [DecimalFormat].
	 */
	private val symbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
		decimalSeparator = '.'
		groupingSeparator = ','
		minusSign = '–'
	}
	
	/**
	 * A mapping from the minimum number necessary for a suffix to apply to the suffix itself for application.
	 * Recommended use is with the [NavigableMap.floorEntry] function, see documentation.
	 */
	private val intSuffix: NavigableMap<Long, String> = TreeMap<Long, String>().apply {
		put(1_000, "k")
		put(1_000_000, "M")
		put(1_000_000_000_000, "T")
	}
	
	/**
	 * Format a given [number] as a [Double] of any size.
	 * The method chooses the most appropriate formatter, either [Format.Small] or [Format.Normal].
	 */
	fun format(number: Double): String = when {
		number < 10 -> Format.Small.format(number)
		else -> Format.Normal.format(number)
	}
	
	/**
	 * Format a given [number] as a [Long].
	 * Negative numbers are not supported with this method.
	 * Uses the [Format.Large] formatter for numbers not less than 1,000.
	 */
	fun format(number: Long): String {
		// This function does not support negative numbers
		if (number < 0) throw Exception("Cannot format negative long")
		
		// Simple case does not need to be handled by DecimalFormat, can be converted directly
		if (number < 1_000) return number.toString()
		
		// Look up the largest multiplier within the map and return it alongside the matching suffix
		// Destruct the return value into two variables for use immediately
		val (long, suffix) = intSuffix.floorEntry(number)
		
		// Divide the number by the largest multiplier found in the map, for the suffix to be applied
		// Format the number using the necessary formatter for large numbers
		val shortNumber = Format.Large.format(number / long)
		
		// Apply the suffix and return the formatted number as a string
		return shortNumber + suffix
	}
	
	/**
	 * Returns a [number] formatted normally with a [suffix] appended to the result.
	 */
	fun format(number: Double, suffix: String) = format(number) + suffix
	/**
	 * Returns a [number] formatted normally with a [suffix] appended to the result.
	 */
	fun format(number: Long, suffix: String) = format(number) + suffix
}

/**
 * Extension function to provide [NumberFormat] formatting capability to the Double.
 */
fun Double.format() = NumberFormat.format(this)

/**
 * Extension function to provide [NumberFormat] formatting with suffix to the Double.
 */
fun Double.format(suffix: String) = NumberFormat.format(this, suffix)

/**
 * Extension function to provide [NumberFormat] formatting to the Long.
 */
fun Long.format() = NumberFormat.format(this)
