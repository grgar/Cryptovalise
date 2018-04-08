package com.georgegarside.cryptovalise.model

import android.graphics.BitmapFactory
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async

object API {
	private class Storage {
		val coins =
				async(start = CoroutineStart.LAZY) { getCoins().associate { it.symbol to it } }
		
		val currencies =
				async(start = CoroutineStart.LAZY) { getCurrencies().associate { it.code to it } }
		
		val prices = ArrayMap()
	}
	
	private var storage = Storage()
	
	var coins = storage.coins
		get() = storage.coins
	
	var currencies = storage.currencies
	
	fun invalidateCache() {
		storage = Storage()
	}
	
	private val fuel = FuelManager.instance
	
	init {
		// Set up base configuration
		fuel.basePath = "https://coin.fyi/"
		fuel.baseHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Accept" to "application/json, text/plain, */*")
	}
	
	data class Coin(val id: Int = 0, val symbol: String = "", val name: String = "", val slug: String = "",
	                val description: String?, var price: Price = Price(), var delta: Delta = Delta(),
	                val supply: Long = 0L, val total: Long = 0L) {
		
		internal val logoPath = fuel.basePath + "uploads/production/coin/icon/$id/$slug.png"
		val logo = async(start = CoroutineStart.LAZY) {
			val bytes = download(logoPath).component1() ?: return@async null
			BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
		}
		
		data class Price(val usd: Double = 0.0, val btc: Double = 0.0) {
			val usdPrice by lazy { "$ " + usd.format() }
			val btcPrice by lazy { "Ḇ " + btc.format() }
			
			val gbpPrice = async(start = CoroutineStart.LAZY) {
				val rate = currencies.await()["GBP"]?.rate
				val gbp = usd * (rate ?: 0.0)
				"£ " + gbp.format()
			}
		}
		
		data class Delta(
				val hour: Pair<Double, Double> = Pair(0.0, 0.0),
				val day: Pair<Double, Double> = Pair(0.0, 0.0),
				val week: Pair<Double, Double> = Pair(0.0, 0.0),
				val cap: Pair<Double, Long> = Pair(0.0, 0L),
				val vol: Pair<Double, Long> = Pair(0.0, 0L),
				val dom: Pair<Double, Int> = Pair(0.0, 0)
		) {
			companion object {
				const val downSymbol = "▽"
				const val upSymbol = "▲"
			}
		}
	}
	
	data class Currency(val code: String = "", val name: String = "", val rate: Double = 0.0)
	
	private inline fun <reified T : Any> call(method: Method = Method.GET, endpoint: String,
	                                          data: List<Pair<String, Any?>>? = null): T {
		// Perform request
		fuel.request(Method.GET, endpoint, data).responseObject<T>().third.fold(success = {
			return it
		}, failure = {
			return mapOf<Any, Any>() as T
		})
	}
	
	private fun download(path: String) = fuel.request(Method.GET, path).response().third
	
	private fun getCoins(): Array<Coin> = call<MapArrayListMap>(endpoint = "coins").let {
		if (!it.containsKey("data")) return@let arrayOf()
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
					symbol = attributes["symbol"] as String,
					name = attributes["currency"] as String,
					slug = attributes["slug"] as String,
					description = attributes["description"] as String,
					price = Coin.Price(
							usd = attributes["price-usd"] as Double,
							btc = attributes["price-btc"] as Double
					),
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
					supply = (attributes["available-supply"] as Double).toLong(),
					total = (attributes["max-supply"] as Double).toLong()
			)
		}?.toTypedArray() ?: arrayOf()
	}
	
	private fun getCurrencies(): Array<Currency> {
		return call<MapArrayListMap>(endpoint = "currencies").let {
			if (!it.containsKey("currencies")) return@let arrayOf()
			it["currencies"]?.map {
				Currency(
						code = it["code"] as String,
						name = it["full_name"] as String,
						rate = it["exchange_rate"] as Double
				)
			}?.toTypedArray() ?: arrayOf()
		}
	}
	
	enum class PriceSeries(val key: String) {
		Price("value_usd"),
		Bitcoin("price_btc"),
		Cap("market_cap_usd");
		
		var data: PointArray? = null
	}
	
	suspend fun getPrices(slug: String): Map<String, PriceSeries> = async {
		val data = call<SimpleMap>(endpoint = "coins/$slug/prices")
		
		arrayOf(PriceSeries.Price, PriceSeries.Bitcoin, PriceSeries.Cap).map {
			
			@Suppress("UNCHECKED_CAST")
			it.data =
					(data[it.key] as? ArrayList<ArrayList<Double>>)
							?.map {
								val x = it[0].toLong()
								val y = it[1]
								Pair(x, y)
							}
							?.toTypedArray()
			
			it.toString() to it
		}.toMap()
		
	}.await()
}
