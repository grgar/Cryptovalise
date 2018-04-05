package com.georgegarside.cryptovalise

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_chart.*

class ChartFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_chart, container, false)
	
	suspend fun loadChart(symbol: String) {
		val slug = API.coins.await()[symbol]?.slug ?: return
		val prices = API.getPrices(slug)
		
		val price = prices[0].data ?: return
		
		val priceList = price.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
		
		val lineDataSet = LineDataSet(priceList, "Prices in USD")
		lineDataSet.axisDependency = YAxis.AxisDependency.LEFT
		
		chart.data = LineData(listOf(lineDataSet))
		chart.invalidate()
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
