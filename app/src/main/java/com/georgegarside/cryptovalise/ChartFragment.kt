package com.georgegarside.cryptovalise

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.PointArray
import com.georgegarside.cryptovalise.presenter.now
import com.georgegarside.cryptovalise.presenter.rgbToSwatch
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChartFragment : Fragment() {
	
	var colour: Palette.Swatch? = null
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_chart, container, false).also {
				colour = rgbToSwatch(arguments?.getInt(CoinDetailActivity.coinColourKey, 0) ?: 0)
				launch(UI) {
					loadChart(arguments?.getString(CoinDetailFragment.coinSymbolKey, "") ?: return@launch)
				}
			}
	
	suspend fun loadChart(symbol: String) {
		val slug = API.coins.await()[symbol]?.slug ?: return
		val prices = API.getPrices(slug)
		
		// Price in USD
/*
		val price = prices[API.PriceSeries.Price.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(API.PriceSeries.Price.toString()).apply {
				axisDependency = YAxis.AxisDependency.LEFT
			}
		}
*/
		/**
		 * Duration of 28 days
		 */
		val rangeStart = (System.currentTimeMillis()) - (1000L * 60 * 60 * 24 * 28)
		
		val price = prices[API.PriceSeries.Price.toString()]
				?.data
				?.filter {
					it.first > rangeStart
				}
				?.toTypedArray()
				?.toEntryList()
				?.toLineDataSet(API.PriceSeries.Price.toString())
				?: return
		
/*
		val bitcoin = prices[API.PriceSeries.Bitcoin.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(it.toString()).apply {
				axisDependency = YAxis.AxisDependency.RIGHT
			}
		}
		
		val cap = prices[API.PriceSeries.Cap.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(it.toString()).apply {
				axisDependency = YAxis.AxisDependency.RIGHT
			}
		}
*/
		
		chart.data = LineData(listOf(price))
		
		setChartStyle(chart)
		
		chart.invalidate()
		
		chartProgress now chart
	}
	
	private fun PointArray.toEntryList() = this.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
	
	private fun List<Entry>.toLineDataSet(label: String) = LineDataSet(this, label)
	
	private fun setChartStyle(chart: LineChart) = chart.apply {
		setDrawBorders(false)
		
		xAxis.apply {
			valueFormatter = dateAxisFormatter
			
			granularity = 1f
			isGranularityEnabled = true
			labelCount = 2
			
			position = XAxis.XAxisPosition.TOP_INSIDE
			
			setDrawAxisLine(false)
			setDrawGridLines(false)
			
			textColor = colour?.bodyTextColor ?: textColor
			
		}
		
		axisLeft.apply {
			setDrawAxisLine(false)
			setDrawGridLines(false)
			
		}
		
		axisRight.apply {
			setDrawAxisLine(false)
			setDrawGridLines(false)
		}
		
		setTouchEnabled(true)
		isDragEnabled = true
	}
	
	private val dateAxisFormatter = IAxisValueFormatter { value, axis ->
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			Instant.ofEpochSecond(value.toLong()).atZone(ZoneId.of("UTC")).format(dateTimeFormatter)
		} else {
			TODO("VERSION.SDK_INT < O")
		}
		
	}
}
