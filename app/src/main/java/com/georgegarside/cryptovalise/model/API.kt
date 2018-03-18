package com.georgegarside.cryptovalise.model

import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.internal.LinkedTreeMap
import java.util.ArrayList

/**
 * API
 *
 * Created by grgarside on 22/02/2018.
 */
object API {
	val coins: Map<String, Coin> = HashMap<String, Coin>()
	
	private fun getArrayDeserializer(key: String) {
		return
	}
	
	private inline fun <reified T : Any> call(method: Method = Method.GET, endpoint: String,
	                                          data: List<Pair<String, Any?>>? = null, key: String? = null): T {
		// Set up base configuration
		val fuel = FuelManager.instance
		fuel.basePath = "https://coin.fyi/"
		fuel.baseHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Accept" to "application/json, text/plain, */*")
		
		// Perform request
		fuel.request(Method.GET, endpoint, data).responseObject<T>().third.fold(success = {
			return it
		}, failure = {
			Log.w("gLog", it.localizedMessage)
			return mapOf<String, String>("error" to it.localizedMessage) as T
		})
	}
	
	fun getCoins(): Array<Coin>? {
		// Perform call to endpoint
		val result = call<ArrayListInMap>(Method.GET, "coins", null, "data")
		
		// Parse response into Coins
		val coins: Array<Coin>? = result["data"]?.map {
			
			// Extract further data as objects
			@Suppress("UNCHECKED_CAST") val contents = it["attributes"] as LinkedTreeMap<String, Any>
			@Suppress("UNCHECKED_CAST") val links = contents["links"] as LinkedTreeMap<String, String>
			
			// Attributes which require special handling e.g. casting
			val id = (it["id"] as String).toInt()
			// Cast anything not being handled specially to strings
			@Suppress("UNCHECKED_CAST") val attributes = contents as LinkedTreeMap<String, String>
			
			// Create coin
			Coin(id, symbol = attributes["symbol"] as String, name = attributes["currency"] as String)
			
		}?.toTypedArray<Coin>()
		
		Log.i("gLog", coins?.get(0).toString())
		return null
		//return call<Array<Coin>>("coins", null)
	}
	
	data class Coin(val id: Int = 0, val symbol: String = "", val name: String = "", val slug: String = "",
	                val description: String = "", val price: Price = Price()) {
		data class Price(val usd: Double = 0.0, val btc: Double = 0.0)
	}
}

private typealias ArrayListInMap = LinkedTreeMap<String, ArrayList<LinkedTreeMap<String, Any>>>

// Data classes
/*open class Deserializable : ResponseDeserializable<String> {
	private inline fun <reified T : Any> jsonToType(content: String, isArray: Boolean): T =
			Gson().fromJson(content, (if (isArray) object : TypeToken<Array<T>>() {}.type else object : TypeToken<T>() {}.type))
	
	override fun deserialize(content: String): String? = jsonToType(content)
}
*/