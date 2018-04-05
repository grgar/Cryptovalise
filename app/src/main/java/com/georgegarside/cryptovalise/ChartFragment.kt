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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_chart.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChartFragment : Fragment() {
	
	var colour: Palette.Swatch? = null
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_chart, container, false)
	
	suspend fun loadChart(symbol: String) {
		val slug = API.coins.await()[symbol]?.slug ?: return
		val prices = API.getPrices(slug)
		
		// Price in USD
		val price = prices[API.PriceSeries.Price.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(it.toString()).apply {
				axisDependency = YAxis.AxisDependency.LEFT
			}
		}
		
		/*val bitcoin = prices[API.PriceSeries.Bitcoin.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(it.toString()).apply {
				axisDependency = YAxis.AxisDependency.RIGHT
			}
		}*/
		
		val cap = prices[API.PriceSeries.Cap.toString()]?.data?.let {
			it.toEntryList().toLineDataSet(it.toString()).apply {
				axisDependency = YAxis.AxisDependency.RIGHT
			}
		}
		
		chart.data = LineData(listOf(price, cap))
		
		setChartStyle(chart)
		
		chart.invalidate()
	}
	
	private fun PointArray.toEntryList() = this.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
	
	private fun List<Entry>.toLineDataSet(label: String) = LineDataSet(this, label)
	
	private fun setChartStyle(chart: LineChart) = chart.apply {
		xAxis.apply {
			valueFormatter = dateAxisFormatter
			
			granularity = 1f
			isGranularityEnabled = true
			
			setDrawAxisLine(false)
			setDrawGridLines(false)
			
			textColor = colour?.rgb ?: textColor
			
			
		}
		
		setTouchEnabled(true)
		isDragEnabled = true
		setScaleEnabled(true)
		setPinchZoom(true)
		
	}
	
	private val dateAxisFormatter = IAxisValueFormatter { value, axis ->
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			Instant.ofEpochSecond(value.toLong()).atZone(ZoneId.of("UTC")).format(dateTimeFormatter)
		} else {
			TODO("VERSION.SDK_INT < O")
		}
		
	}
	
	companion object {
		/**
		 * Factory method to create a new instance
		 */
		// TODO: Rename and change types and number of parameters
		fun newInstance(param1: String, param2: String) =
				ChartFragment().apply {
					arguments = Bundle().apply {
						/*
												putString(ARG_PARAM1, param1)
												putString(ARG_PARAM2, param2)
						*/
					}
				}
	}
}
