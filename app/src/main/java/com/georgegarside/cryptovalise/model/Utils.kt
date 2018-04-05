package com.georgegarside.cryptovalise.model

import com.google.gson.internal.LinkedTreeMap
import java.util.*

/**
 * A [LinkedTreeMap], from [String] to [ArrayList] of [LinkedTreeMap], from [String] to [Any].
 */
typealias MapArrayListMap =
		LinkedTreeMap<
				String,
				ArrayList<
						LinkedTreeMap<
								String,
								Any
								>
						>
				>

/**
 *
 */
typealias ArrayMap =
		LinkedTreeMap<
				String,
				Array<
						Array<
								String
								>
						>
				>

typealias SimpleMap =
		LinkedTreeMap<
				String,
				Any
				>

typealias PointArray =
		Array<
				Pair<
						Long,
						Double
						>
				>

/**
 * Extension function to provide [NumberFormat] formatting capability to the Double.
 */
fun Double.format() = NumberFormat.format(this)

/**
 * Extension function to use more customised [NumberFormat] formatting by specifying the [format].
 */
fun Double.format(format: NumberFormat) = format.format(this)

/**
 * Extension function for [NumberFormat] with a [suffix].
 */
fun Double.format(format: NumberFormat, suffix: String) = format.format(this, suffix)

/**
 * Extension function to provide [NumberFormat] formatting to the Long.
 */
fun Long.format() = NumberFormat.format(this)
