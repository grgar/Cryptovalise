package com.georgegarside.cryptovalise.model

import android.graphics.BitmapFactory
import android.net.Uri
import com.georgegarside.cryptovalise.model.Coin.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async

/**
 * Data class for a single [Coin] returned from the [API] and mapped to this class with the [API.getCoins] method.
 *
 * The [API.call] is likely to return more information about a coin than this app requires, so it is not necessary to
 * hold all of this information. This class has values for the useful information which should be kept about the coin.
 * There are also a number of nested data classes which hold data to be kept together: [Price], [Delta] & [Links].
 *
 * A specific coin which the API is queried for may not have all the necessary data to complete this data class,
 * or the API may change its response and a field be unable to be mapped to a value in the constructor. Therefore, each
 * constructor parameter has a default value which is used if none is provided to the constructor, and commonly missing
 * fields are marked optional, such as [description].
 */
data class Coin(
		/**
		 * The server-side ID for the coin. This is not sequential, however the ID is unique across all coins.
		 */
		val id: Int = 0,
		/**
		 * The symbol for the coin, usually 3 or 4 characters in length. These are unique across all coins.
		 */
		val symbol: String = "",
		/**
		 * The name of the coin.
		 */
		val name: String = "",
		/**
		 * The coin's canonical name as a single word in lowercase.
		 */
		val slug: String = "",
		/**
		 * A description of the coin, derived from the first paragraph of the Wikipedia page for the coin if one exists,
		 * or other canonical source such as Google Knowledge Graph.
		 */
		val description: String?,
		/**
		 * A [Price] object containing the latest price information for the coin.
		 */
		var price: Price = Price(),
		/**
		 * A [Delta] object containing the latest changes in price over recent time, such as the previous hour or day.
		 */
		var delta: Delta = Delta(),
		/**
		 * The number of coins in circulation.
		 */
		val supply: Long = 0L,
		/**
		 * The total number of coins which could ever be made available, or 0 if the supply is unlimited.
		 */
		val total: Long = 0L,
		/**
		 * A [Links] object containing links to associated websites and URLs for this coin.
		 */
		val links: Links = Links()
) {
	
	/**
	 * The computed path to the coin's logo, which can be downloaded from this full URL.
	 */
	internal val logoPath = API.basePath + "uploads/production/coin/icon/$id/$slug.png"
	
	/**
	 * A deferred Bitmap of the coin's logo, performs [API.download] on the [logoPath] and returns the bitmap from the
	 * downloaded byte array.
	 */
	val logo = async(start = CoroutineStart.LAZY) {
		val bytes = API.download(logoPath) ?: return@async null
		BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
	}
	
	/**
	 * Data class representing the price of a coin in different formats.
	 */
	data class Price(
			/**
			 * The present value of the coin in USD.
			 */
			val usd: Double = 0.0,
			/**
			 * The present value of the coin in BTC, calculated as how much BTC can be purchased with 1 coin's USD value.
			 */
			val btc: Double = 0.0
	) {
		/**
		 * The [usd] formatted as a price with currency icon and the custom extension function [Double.format].
		 */
		val usdPrice by lazy { "$ " + usd.format() }
		/**
		 * The [btc] formatted like [usdPrice].
		 */
		val btcPrice by lazy { "Ḇ " + btc.format() }
		
		/**
		 * The [usd] converted to GBP according to the current exchange rate obtained from the GBP [Currency].
		 * Querying the current USD-GBP exchange rate is an asynchronous, so this is Deferred.
		 */
		val gbpPrice = async(start = CoroutineStart.LAZY) {
			val rate = API.currencies.await()["GBP"]?.rate
			val gbp = usd * (rate ?: 0.0)
			"£ " + gbp.format()
		}
	}
	
	/**
	 * Data class representing the change in value or other price-related metrics a coin over a recent period, such as a
	 * day or week. Each delta is given as a [Pair] where
	 * - [Pair.first] is a [Double] representing the percentage change in the given time period for that value, and
	 * - [Pair.second] is a [Double] or [Long] representing the value itself for that time period.
	 *
	 * These deltas can be formatted using [NumberFormat.Delta] using the symbols in [Delta.Symbols].
	 */
	data class Delta(
			/**
			 * The change in value of the coin in the past hour (60 minutes).
			 */
			val hour: Pair<Double, Double> = Pair(0.0, 0.0),
			/**
			 * The change in value of the coin in the past day (the last 24 hour period, not purely from the last midnight).
			 */
			val day: Pair<Double, Double> = Pair(0.0, 0.0),
			/**
			 * The change in value of the coin in the past week (a 7 * 24 hour period).
			 */
			val week: Pair<Double, Double> = Pair(0.0, 0.0),
			/**
			 * The market capitalisation of the coin and the percentage change in the past 24 hours.
			 */
			val cap: Pair<Double, Long> = Pair(0.0, 0L),
			/**
			 * The total volume of transactions processed for the coin in the past 24 hours and the percentage change of this
			 * value from the previous 24 hour period.
			 */
			val vol: Pair<Double, Long> = Pair(0.0, 0L),
			/**
			 * The coin's current rank amongst the coins (1-indexed) and the change in percentage domination of the coin
			 * versus other coins being tracked in the past 24 hour period.
			 */
			val dom: Pair<Double, Int> = Pair(0.0, 0)
	) {
		/**
		 * Unicode characters used as symbols to represent a delta change either [upSymbol] or [downSymbol].
		 */
		companion object Symbols {
			/**
			 * An upward filled triangle representing an increasing delta.
			 */
			const val upSymbol = "▲"
			/**
			 * A downward outline-only triangle representing a decreasing delta.
			 */
			const val downSymbol = "▽"
		}
	}
	
	/**
	 * Data class holding associated links for a coin which the user may wish to navigate to for more information about a
	 * coin. Each link is a [Uri] to an external resource, such as a webpage.
	 */
	data class Links(
			/**
			 * The main website for the coin.
			 */
			val website: Uri? = null,
			/**
			 * The whitepaper for the coin's proposal, usually a link to a PDF resource.
			 */
			val whitepaper: Uri? = null
	)
}
