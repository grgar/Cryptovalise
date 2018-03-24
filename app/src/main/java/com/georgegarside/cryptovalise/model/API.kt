package com.georgegarside.cryptovalise.model

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.experimental.async
import java.text.DecimalFormat

/**
 * API
 *
 * Created by grgarside on 22/02/2018.
 */
object API {
	val coins by lazy { async { getCoins().associate { it.symbol to it } } }
	
	val currencies by lazy { async { getCurrencies().associate { it.code to it } } }
	
	data class Coin(val id: Int = 0, val symbol: String = "", val name: String = "", val slug: String = "",
	                val description: String = "", val price: Price = Price()) {
		data class Price(val usd: Double = 0.0, val gbp: Double = 0.0, val btc: Double = 0.0) {
			private val large by lazy { DecimalFormat("@@@@@#") }
			private val small by lazy { DecimalFormat("0.####") }
			private fun format(number: Double) = if (number < 10) small.format(number) else large.format(number)
			val usdPrice by lazy { "$ " + format(usd) }
			val gbpPrice by lazy { "£ " + format(gbp) }
			val btcPrice by lazy { "BTC " + format(btc) }
		}
	}
	
	data class Currency(val code: String = "", val name: String = "", val rate: Double = 0.0)
	
	private inline fun <reified T : Any> call(method: Method = Method.GET, endpoint: String,
	                                          data: List<Pair<String, Any?>>? = null): T {
		// Set up base configuration
		val fuel = FuelManager.instance
		fuel.basePath = "https://coin.fyi/"
		fuel.baseHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Accept" to "application/json, text/plain, */*")
		
		// Perform request
		fuel.request(Method.GET, endpoint, data).responseObject<T>().third.fold(success = {
			return it
		}, failure = {
			return mapOf<String, String>("error" to it.localizedMessage) as T
		})
	}
	
	private fun getCoins(): Array<Coin> {
		// Perform call to endpoint
		val result = call<ArrayListInMap>(Method.GET, "coins")
		
		// Parse response into Coins
		return result["data"]?.map {
			
			// Extract further data as objects
			@Suppress("UNCHECKED_CAST") val attributes = it["attributes"] as LinkedTreeMap<String, Any>
			@Suppress("UNCHECKED_CAST") val links = attributes["links"] as LinkedTreeMap<String, String>
			
			// Attributes which require special handling e.g. casting
			val id = (it["id"] as String).toInt()
			
			// Create coin
			Coin(id,
					symbol = attributes["symbol"] as String,
					name = attributes["currency"] as String,
					slug = attributes["slug"] as String,
					description = attributes["description"] as String,
					price = Coin.Price(
							usd = attributes["price-usd"] as Double,
							btc = attributes["price-btc"] as Double
					)
			)
			
		}?.toTypedArray<Coin>() ?: arrayOf()
	}
	
	private fun getCurrencies(): Array<Currency> {
		return call<ArrayListInMap>(Method.GET, "currencies")["currencies"]?.map {
			Currency(
					code = it["code"] as String,
					name = it["full_name"] as String,
					rate = it["exchange_rate"] as Double
			)
		}?.toTypedArray<Currency>() ?: arrayOf()
	}
}
