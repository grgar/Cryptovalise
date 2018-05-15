package com.georgegarside.cryptovalise

import android.graphics.PorterDuff
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georgegarside.cryptovalise.model.API
import com.georgegarside.cryptovalise.model.NumberFormat
import com.georgegarside.cryptovalise.model.PointArray
import com.georgegarside.cryptovalise.model.format
import com.georgegarside.cryptovalise.presenter.now
import com.georgegarside.cryptovalise.presenter.toSwatch
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * The fragment containing the chart view. Inflates [R.layout.fragment_chart] and loads the date with [loadChart] using
 * the given [colour] as the theme. The [setChartStyle] will format the chart correctly to be displayed.
 */
class ChartFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
			inflater.inflate(R.layout.fragment_chart, container, false).also {
				
				// Set the colour palette for this fragment by creating a swatch from the passed colour
				colour = arguments?.getInt(CoinDetailActivity.coinColourKey)?.let { rgb ->
					if (rgb != 0) rgb.toSwatch() else null
				}
				
				// Load the chart content asynchronously
				launch(UI) {
					colour?.titleTextColor?.let {
						chartProgress?.indeterminateDrawable?.setColorFilter(it, PorterDuff.Mode.SRC_IN)
					}
					
					// Determine what coin to show
					val symbol = arguments?.getString(CoinDetailFragment.coinSymbolKey) ?: return@launch
					// Load the content into the view
					loadChart(symbol, API.PriceSeries.Price)
				}
			}
	
	/**
	 * The colour theme of the chart fragment, used to colour the line and content to be visible against the background.
	 */
	private var colour: Palette.Swatch? = null
	
	/**
	 * Load the given [series] of data for [symbol] into the [chart].
	 */
	suspend fun loadChart(symbol: String, series: API.PriceSeries) {
		chart now chartProgress
		
		val slug = API.coins.await()[symbol]?.slug ?: return
		val prices = API.getPrices(slug)
		
		val price = prices[series.toString()]
				?.data
				?.toEntryList()
				?.toLineDataSet(series.toString())
				?.also {
					setLineStyle(it)
				}
				?: return
		
		chart.apply {
			data = LineData(listOf(price))
			
			setChartStyle(chart)
			
			invalidate()
			setVisibleXRangeMaximum((visibleXRange * 12.5).toFloat())
			
			chartProgress now this
			
			moveViewToAnimated(Float.MAX_VALUE, Float.MAX_VALUE, axisLeft.axisDependency, 1000)
		}
	}
	
	/**
	 * Map a [PointArray] to a list of [Entry] to be displayed on the graph.
	 */
	private fun PointArray.toEntryList() = this.map { Entry((it.first / 1000).toFloat(), it.second.toFloat()) }
	
	/**
	 * Convert a [List] of [Entry] to a [LineDataSet].
	 */
	private fun List<Entry>.toLineDataSet(label: String) = LineDataSet(this, label)
	
	/**
	 * Set the line styles for the [lineDataSet] from the [colour] scheme of the chart.
	 */
	private fun setLineStyle(lineDataSet: LineDataSet) = lineDataSet.apply {
		color = colour?.bodyTextColor
				?: context?.let { ContextCompat.getColor(it, android.R.color.black) }
				?: color
		setDrawCircles(false)
		setDrawValues(false)
		setDrawIcons(false)
	}
	
	/**
	 * Set the style of the [chart] with formatting for X axis, left and right axis, and interactivity styling.
	 */
	private fun setChartStyle(chart: LineChart) = chart.apply chart@{
		// The chart does not need borders since it is full bleed on device
		setDrawBorders(false)
		setDrawGridBackground(false)
		
		// The title of the graph is the coin title which is already displayed
		setNoDataText("")
		setDescription("")
		
		// As the graph is scrolled horizontally, keep the left axis minimum and maximum at the trough and peak of the data
		// currently visible within the region
		isAutoScaleMinMaxEnabled = true
		setVisibleXRangeMaximum(60f * 60 * 24 * 28)
		
		// Remove margins from graph, full bleed
		setViewPortOffsets(0f, 0f, 0f, 0f)
		setExtraOffsets(0f, 0f, 0f, 0f)
		
		// Time axis
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
		
		// Price axis
		axisLeft.apply {
			setDrawAxisLine(false)
			setDrawGridLines(false)
			setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
			
			textColor = colour?.bodyTextColor ?: textColor
			
			valueFormatter = priceValueFormatter
		}
		
		axisRight.isEnabled = false
		
		// Only one series currently shown
		legend.isEnabled = false
		
		// Interactivity
		setTouchEnabled(true)
		isDragXEnabled = true
		isScaleXEnabled = true
		isDoubleTapToZoomEnabled = false
		isHighlightPerDragEnabled = false
		isHighlightPerTapEnabled = true
		
		// Show data at tapped value on graph as description (small text beneath), also draw axis lines at tapped point
		setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
			override fun onNothingSelected() = this@chart.setDescription("")
			
			override fun onValueSelected(entry: Entry?, highlight: Highlight?) = entry?.let {
				this@chart.setDescription("$${entry.y} on ${dateFormat(entry.x, "yyyy-MM-dd HH:mm")}")
			} ?: Unit
		})
	}
	
	/**
	 * Set the [Chart]'s description to the given [string] with appropriate formatting (size and colour).
	 */
	private fun Chart<*>.setDescription(string: String) {
		description = Description().apply {
			text = string
			textSize = 12f
			textColor = colour?.bodyTextColor ?: textColor
		}
	}
	
	/**
	 * Convert a given [value] into a string for the price it represents.
	 */
	private val priceValueFormatter = IAxisValueFormatter { value, _ ->
		"$" + value.toDouble().format(NumberFormat.Large)
	}
	
	/**
	 * Convert a given [value] into a string for the date it represents.
	 */
	private val dateAxisFormatter = IAxisValueFormatter { value, _ ->
		dateFormat(value, "yyyy-MM-dd")
	}
	
	/**
	 * Format a given [value] using the [datePattern]. This chooses the best method depending on the Android version.
	 */
	private fun dateFormat(value: Float, datePattern: String) = when {
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
