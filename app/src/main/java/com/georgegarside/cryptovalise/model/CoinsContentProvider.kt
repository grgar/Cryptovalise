package com.georgegarside.cryptovalise.model

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.georgegarside.cryptovalise.BuildConfig

class CoinsContentProvider : ContentProvider() {
	
	companion object {
		private const val authority = "com.georgegarside.cryptovalise"
		val base = Uri.parse("content://$authority")!!
	}
	
	private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
	private val db: SQLiteDatabase by lazy { DBOpenHelper(context).writableDatabase }
	
	/**
	 * Operations which can be performed on the database, depending on the URI
	 */
	private enum class Operations(val table: String) {
		ALL("coin"), SINGLE("coin");
		
		companion object {
			// Cached result of values since values are immutable
			val values = Operations.values()
			
			fun tableName(ordinal: Int): String {
				Log.d("gLog", ordinal.toString())
				Log.d("gLog", values.toString())
				Log.d("gLog", values[ordinal].toString())
				return values[ordinal].table
			}
		}
	}
	
	init {
		// Set up URI paths
		uriMatcher.addURI(authority, Operations.ALL.table, Operations.ALL.ordinal)
		uriMatcher.addURI(authority, "${Operations.SINGLE.table}/#", Operations.SINGLE.ordinal)
	}
	
	/**
	 * Returns a [db]'s [DBOpenHelper.Table] from a [uri] using the [uriMatcher]
	 */
	private fun dbTable(uri: Uri) =
			DBOpenHelper.findTable(
					Operations.tableName(
							uriMatcher.match(uri)))
	
	// Override of methods from ContentProvider class
	
	/**
	 * Lazy initialisation of [db] as recommended in documentation, so don't initialise here
	 */
	override fun onCreate(): Boolean = true
	
	/**
	 * Custom MIME type for this application
	 */
	override fun getType(uri: Uri): String? = "${BuildConfig.APPLICATION_ID}.item"
	
	override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
	                   sortOrder: String?): Cursor? {
		// Get table to perform query on
		return dbTable(uri)?.let {
			return db.query(it.name, it.columns, selection, selectionArgs, null, null, sortOrder)
		}
	}
	
	/**
	 * Insert [values] into the [db] in the table determined by parsing the [uri]
	 *
	 * Returns [Uri] pointing to newly inserted values
	 */
	override fun insert(uri: Uri, values: ContentValues?): Uri? {
		// Get table to insert data into
		return dbTable(uri)?.let {
			// Perform insertion and get ID for row just inserted
			val insertionId = db.insert(it.name, null, values)
			// Convert ID into Uri to return
			Uri.parse("${it.name}/$insertionId")
		}
	}
	
	override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
		// Get table to perform update operation on
		return dbTable(uri)?.let {
			db.update(it.name, values, selection, selectionArgs)
		}
		// If no table was found, no rows were updated
				?: 0
	}
	
	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
		// Get table to perform delete operation on
		return dbTable(uri)?.let {
			// Tell database to delete matching rows
			db.delete(it.name, selection, selectionArgs)
		}
		// If no table was found, no rows were deleted
				?: 0
	}
}

