package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_chart.*

class ChartFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_chart, container, false)
	
	suspend fun loadChart(symbol: String) {
		val slug = API.coins.await()[symbol]?.slug ?: return
		val prices = API.getPrices(slug)
		
		// Price in USD
		val price = prices[API.PriceSeries.Price.toString()]?.data?.let {
			val list = it.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
			
			LineDataSet(list, it.toString()).apply {
				axisDependency = YAxis.AxisDependency.LEFT
			}
		}
		
		val cap = prices[API.PriceSeries.Cap.toString()]?.data?.let {
			val list = it.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
			
			LineDataSet(list, it.toString()).apply {
				axisDependency = YAxis.AxisDependency.RIGHT
			}
		}
		
		chart.data = LineData(listOf(price, cap))
		
		setChartStyle(chart)
		
		chart.invalidate()
	}
	
	private fun setChartStyle(chart: LineChart) {
	}
	
	val dateAxisFormatter = IAxisValueFormatter { value, axis -> TODO("Implement") }
	
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
