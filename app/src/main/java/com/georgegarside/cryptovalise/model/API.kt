package com.georgegarside.cryptovalise.model

import android.net.Uri
import com.georgegarside.cryptovalise.model.API.coins
import com.georgegarside.cryptovalise.model.API.currencies
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async

/**
 * API to obtain data from the server. This class provides the ability to get [coins] and [currencies], and makes
 * supplementary HTTP calls necessary to get all the data needed.
 *
 * This singleton caches coin, currency, price and download data in memory. Since the data accessed by this app is very
 * small, it is unnecessary to use the overhead of a disk cache in this case, however it is something which could be
 * added in the future. Other downloads such as those performed by Chrome Custom Tabs for downloading the whitepaper
 * does not go through this class and is stored on disk semi-permanently in a user-accessible location.
 */
object API {
	/**
	 * Make a call to the given [endpoint] using the REST [method], passing [data] and returning [T].
	 *
	 * This generic function will return the response object as the given type. This is useful as the method can be used
	 * for quick conversion to objects from a JSON structure into a [LinkedTreeMap], while also being used for simple
	 * [ByteArray] downloads (wrapped in the [download] function for cache access).
	 */
	private suspend inline fun <reified T : Any> call(method: Method = Method.GET, endpoint: String,
	                                                  data: List<Pair<String, Any?>>? = null): T = async {
		// Perform request
		fuel.request(method, endpoint, data).responseObject<T>().third.fold(success = {
			// Return successful response, converted to T (reified)
			it
		}, failure = {
			// Fail with empty data
			mapOf<Any, Any>() as T
		})
	}.await()
	
	/**
	 * Perform a simple [call] to return a [ByteArray] of the [path]. This download is cached in [Storage.downloads] and
	 * the cache is automatically used where possible in future downloads, keyed by the given [path]. This function
	 * performs the call asynchronously and will suspend the coroutine until the download is complete.
	 */
	suspend fun download(path: String) = async {
		// Attempt to get file from cache storage
		storage.downloads[path]
		
		// If not found in cache, perform request and return success response
				?: fuel.request(Method.GET, path).response().third.component1()
						
						// After request was successful, store response in cache
						?.also { storage.downloads[path] = it }
				
				// If no valid response, return empty
				?: ByteArray(0)
	}.await()
	
	/**
	 * Get an array of coins each as an instance of [Coin]. This data is always the latest available from the server and
	 * the response should be cached by the caller of this function. Suspending function performs the network [call]
	 * asynchronously and suspends the containing coroutine. The network call is performed requesting a [MapArrayListMap].
	 */
	private suspend fun getCoins(): Array<Coin> = call<MapArrayListMap>(endpoint = "coins").let {
		// Check if invalid response received and return empty
		if (!it.containsKey("data")) return@let arrayOf()
		
		// For each coin data as JSON object (LinkedTreeMap), map the object to a Coin
		it["data"]?.map {
			
			// Extract further data as objects
			@Suppress("UNCHECKED_CAST")
			val attributes = it["attributes"] as LinkedTreeMap<String, Any>
			@Suppress("UNCHECKED_CAST")
			val links = attributes["links"] as LinkedTreeMap<String, String>
			
			// Attributes which require special handling e.g. casting
			val id = (it["id"] as String).toInt()
			
			// Create coin
			Coin(id,
					// Basic info
					symbol = attributes["symbol"] as String,
					name = attributes["currency"] as String,
					slug = attributes["slug"] as String,
					description = attributes["description"] as String,
					// Latest prices
					price = Coin.Price(
							usd = attributes["price-usd"] as Double,
							btc = attributes["price-btc"] as Double
					),
					// Latest price deltas
					delta = Coin.Delta(
							hour = Pair(attributes["percent-change-1h"] as Double, attributes["point-change-1h"] as Double),
							day = Pair(attributes["percent-change-24h"] as Double, attributes["point-change-24h"] as Double),
							week = Pair(attributes["percent-change-7d"] as Double, attributes["point-change-7d"] as Double),
							cap = Pair(attributes["market-cap-percent-change"] as Double,
									(attributes["market-cap-usd"] as Double).toLong()),
							vol = Pair(attributes["volume-percent-change"] as Double,
									(attributes["volume-24h-usd"] as Double).toLong()),
							dom = Pair(attributes["dominance-percent-change"] as Double, (attributes["rank"] as Double).toInt())
					),
					// Total values
					supply = (attributes["available-supply"] as Double).toLong(),
					total = (attributes["max-supply"] as Double).toLong(),
					// Links to associated URLs
					links = Coin.Links(
							website = links["website"]?.let { if (!it.isBlank()) Uri.parse(it) else null },
							whitepaper = links["whitepaper"]?.let { if (!it.isBlank()) Uri.parse(it) else null }
					)
			)
		}
				// List to Array
				?.toTypedArray()
		// If error, return empty array
				?: arrayOf()
	}
	
	/**
	 * Get an array of currencies with each an instance of [Currency]. See [getCoins] for more details on the logistics.
	 */
	private suspend fun getCurrencies() = call<MapArrayListMap>(endpoint = "currencies").let {
		if (!it.containsKey("currencies")) return@let arrayOf<Currency>()
		it["currencies"]?.map {
			Currency(
					code = it["code"] as String,
					name = it["full_name"] as String,
					rate = it["exchange_rate"] as Double
			)
		}?.toTypedArray() ?: arrayOf()
	}
	
	/**
	 * Series of historical price information available from the server. See individual documentation per series.
	 */
	enum class PriceSeries(val key: String) {
		/**
		 * The historical market value of the coin in USD trading.
		 */
		Price("value_usd"),
		/**
		 * The historical value of the coin converted to equivalent BTC through USD with exchange rate of each coin at the
		 * time of the value being recorded (i.e. ‘on this day, how much BTC could one obtain with 1 coin on USD market?’)
		 */
		Bitcoin("price_btc"),
		/**
		 * The historical market capitalisation of the coin, using the market value on USD trading, given in USD.
		 */
		Cap("market_cap_usd");
		
		/**
		 * Storage of the returned data as an array of points (typealias [PointArray]).
		 * - [Pair.first] is the timestamp when the value was recorded, given as [Long] GMT seconds since epoch.
		 * - [Pair.second] is the value recorded as [Double].
		 */
		var data: PointArray? = null
	}
	
	/**
	 * Get all historical prices available for a [Coin] given the [slug] from [Coin.slug]. Returns a map from the
	 * [PriceSeries.toString] to the [PriceSeries] itself where the [PriceSeries.data] can be extracted. The cache is
	 * queried first, and if missing a successful response to the [call] is stored in the cache for this coin.
	 */
	
	suspend fun getPrices(slug: String): Map<String, PriceSeries> = async {
		/**
		 * The data response to the network call.
		 */
		val data = call<SimpleMap>(endpoint = "coins/$slug/prices")
		
		// Save each series's data to the PriceSeries storage
		PriceSeries.values().map {
			
			@Suppress("UNCHECKED_CAST")
			it.data =
					(data[it.key] as? ArrayList<ArrayList<Double>>)
							?.map {
								/**
								 * The timestamp when the value was recorded, given as [Long] GMT seconds since epoch.
								 */
								val x = it[0].toLong()
								/**
								 * The value recorded at the timestamp given as [Double].
								 */
								val y = it[1]
								// Save these x and y values as a pair in the array
								Pair(x, y)
							}
							// Immutable array
							?.toTypedArray()
			
			// Perform the mapping from the PriceSeries name to the data contents
			it.toString() to it
		}.toMap()
	}.await()
	
	/**
	 * The server is located at [basePath]. One can switch to the staging or alternative server by changing the path const.
	 */
	const val basePath = "https://coin.fyi/"
	
	/**
	 * Temporary storage for this app session to hold a cache of data in memory.
	 */
	private class Storage {
		/**
		 * Internal coin data storage, see [API.coins].
		 */
		val coins =
				async(start = CoroutineStart.LAZY) { getCoins().associate { it.symbol to it } }
		
		/**
		 * Internal currency data storage, see [API.currencies].
		 */
		val currencies =
				async(start = CoroutineStart.LAZY) { getCurrencies().associate { it.code to it } }
		
		/**
		 * Internal cache in raw [ByteArray] [HashMap] keyed by the download path.
		 */
		val downloads: HashMap<String, ByteArray> = HashMap()
	}
	
	/**
	 * An instance of [Storage] for storing data. This storage is not made accessible directly, only through getters.
	 * Even though this is not accessible directly outside of this singleton, the cache can be reset externally by calling
	 * [invalidateCache].
	 */
	private var storage = Storage()
	
	/**
	 * Getter for coin data as an immutable [Map] of [Coin] keyed by the [Coin.symbol].
	 */
	var coins = storage.coins
		get() = storage.coins
	
	/**
	 * Getter for currencies as an immutable [Map] of [Currency] keyed by the [Currency.code].
	 */
	var currencies = storage.currencies
		get() = storage.currencies
	
	/**
	 * Empty the cache within this singleton.
	 */
	fun invalidateCache() {
		storage = Storage()
	}
	
	/**
	 * The singleton instance of Fuel for REST HTTP [call]s made.
	 */
	private val fuel = FuelManager.instance
	
	// Init is called the first time this singleton is accessed
	init {
		// Set up base configuration
		fuel.basePath = basePath
		fuel.baseHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Accept" to "application/json, text/plain, */*")
	}
}
