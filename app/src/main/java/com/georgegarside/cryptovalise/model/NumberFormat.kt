package com.georgegarside.cryptovalise.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Number formatter for formatting [Double] or [Long] to be displayed in UI. This custom formatter utilises
 * [DecimalFormat] to provide standardised formatting of numbers.
 *
 * NumberFormat supports formatting different sizes of numbers in different ways. The given pattern for an enum
 * determines how those numbers passed to the [NumberFormat.format] function will be formatted and returned as a string.
 */
enum class NumberFormat(pattern: String) {
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
	Large("#,###,###,###,###,###,##0"),
	/**
	 * Format a number as a delta change with up/down triangle.
	 */
	Delta("▲#,##0.0;▽#");
	
	/**
	 * An instance of a [DecimalFormat] with the [symbols] set as defined in [NumberFormat].
	 */
	private val decimalFormat = DecimalFormat(pattern).apply {
		decimalFormatSymbols = symbols
	}
	
	/**
	 * The [DecimalFormatSymbols] used for formatting numbers with [DecimalFormat].
	 */
	open val symbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
		minusSign = '–'
	}
	
	/**
	 * Perform the formatting of a [number] using the chosen [NumberFormat] from the enum.
	 * Returns a string formatted representation of the number given.
	 */
	fun format(number: Number): String = decimalFormat.format(number)
	
	/**
	 * Returns a formatted [number] using the [NumberFormat] and appending the [suffix].
	 */
	fun format(number: Number, suffix: String): String = format(number) + suffix
	
	companion object {
		/**
		 * Format a given [number] as a [Double] of any size.
		 * The method chooses the most appropriate formatter, either [Format.Small] or [Format.Normal].
		 */
		fun format(number: Double): String = when {
			number < 10 -> Small.format(number)
			else -> Normal.format(number)
		}
		
		/**
		 * Format a given [number] as a [Long].
		 * Negative numbers are not supported with this method.
		 * Uses the [NumberFormat.format] formatter for numbers not less than 1,000.
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
			val shortNumber = Large.format(number / long)
			
			// Apply the suffix and return the formatted number as a string
			return shortNumber + suffix
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
	}
}
