package com.georgegarside.cryptovalise

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
					true //it.first > rangeStart
				}
				?.toTypedArray()
				?.toEntryList()
				?.toLineDataSet(API.PriceSeries.Price.toString())?.also {
					setLineStyle(it)
				}
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
		
		chart.apply {
			data = LineData(listOf(price))
			
			setChartStyle(chart)
			
			invalidate()
			moveViewToX(Float.MAX_VALUE)
			
			chartProgress now this
		}
	}
	
	private fun PointArray.toEntryList() = this.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
	
	private fun List<Entry>.toLineDataSet(label: String) = LineDataSet(this, label)
	
	private fun setLineStyle(lineDataSet: LineDataSet) = lineDataSet.apply {
		color = colour?.titleTextColor ?: return@apply
		setDrawCircles(false)
		setDrawValues(false)
		setDrawIcons(false)
	}
	
	private fun setChartStyle(chart: LineChart) = chart.apply {
		setDrawBorders(false)
		setDrawGridBackground(false)
		
		setNoDataText("")
		description = Description().apply desc@{
			text = context.getString(R.string.coin_detail_chart_timeframe)
			textSize = 12f
			textColor = colour?.bodyTextColor ?: textColor
		}
		
		isAutoScaleMinMaxEnabled = true
		setVisibleXRangeMaximum(60f * 60 * 24 * 28)
		
		xAxis.apply {
			valueFormatter = dateAxisFormatter
			
			granularity = 60f * 15
			isGranularityEnabled = true
			labelCount = 1
			
			position = XAxis.XAxisPosition.TOP_INSIDE
			
			setDrawAxisLine(false)
			setDrawGridLines(false)
			
			textColor = colour?.bodyTextColor ?: textColor
			textSize = 12f
		}
		
		axisLeft.apply {
			setDrawAxisLine(false)
			setDrawGridLines(false)
			setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
			
			textColor = colour?.bodyTextColor ?: textColor
			
			valueFormatter = priceValueFormatter
		}
		
		axisRight.isEnabled = false
		
		legend.isEnabled = false
		
		setTouchEnabled(true)
		isDragXEnabled = true
		isScaleXEnabled = true
		isDoubleTapToZoomEnabled = false
		isHighlightPerDragEnabled = false
		isHighlightPerTapEnabled = false
		
		animateX(2000, Easing.EasingOption.EaseOutQuad)
	}
	
	private val dateAxisFormatter = IAxisValueFormatter { value, _ ->
		val datePattern = "yyyy-MM-dd"
		
		when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
				Instant
						.ofEpochSecond(value.toLong())
						.atZone(ZoneId.of(TimeZone.GMT_ZONE.id))
						.format(DateTimeFormatter.ofPattern(datePattern))
			}
			
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
				SimpleDateFormat(datePattern, Locale.ENGLISH)
						.apply { timeZone = TimeZone.GMT_ZONE }
						.format(Date(value.toLong()))
			}
			
			else -> {
				java.text.SimpleDateFormat(datePattern, Locale.ENGLISH)
						.apply { timeZone = java.util.TimeZone.getTimeZone("GMT") }
						.format(Date(value.toLong()))
			}
		}
	}
	
	private val priceValueFormatter = IAxisValueFormatter { value, axis ->
		"$" + LargeValueFormatter().getFormattedValue(value, axis).capitalize()
	}
}
