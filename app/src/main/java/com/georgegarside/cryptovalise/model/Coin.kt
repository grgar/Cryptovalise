package com.georgegarside.cryptovalise.model

import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async

data class Coin(val id: Int = 0, val symbol: String = "", val name: String = "", val slug: String = "",
                val description: String?, var price: Price = Price(), var delta: Delta = Delta(),
                val supply: Long = 0L, val total: Long = 0L,
                val links: Links) {
	
	internal val logoPath = API.basePath + "uploads/production/coin/icon/$id/$slug.png"
	val logo = async(start = CoroutineStart.LAZY) {
		val bytes = API.download(logoPath) ?: return@async null
		BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
	}
	
	data class Price(val usd: Double = 0.0, val btc: Double = 0.0) {
		val usdPrice by lazy { "$ " + usd.format() }
		val btcPrice by lazy { "Ḇ " + btc.format() }
		
		val gbpPrice = async(start = CoroutineStart.LAZY) {
			val rate = API.currencies.await()["GBP"]?.rate
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
	
	data class Links(
			val website: Uri? = null,
			val whitepaper: Uri? = null
	)
}
