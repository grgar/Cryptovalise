package com.georgegarside.cryptovalise.model

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.georgegarside.cryptovalise.BuildConfig

class CoinContentProvider : ContentProvider() {
	
	companion object {
		private const val authority = "com.georgegarside.cryptovalise"
		val baseUri = Uri.parse("content://$authority")!!
	}
	
	private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
	private val dbOpenHelper by lazy { DBOpenHelper(context) }
	private val db: SQLiteDatabase by lazy { dbOpenHelper.writableDatabase }
	
	/**
	 * Operation which can be performed on the database, depending on the URI
	 */
	enum class Operation(val table: String, val uri: Uri) {
		ALL("Coin", Uri.withAppendedPath(baseUri, "coin")),
		SINGLE("Coin", Uri.withAppendedPath(baseUri, "coin"));
	}
	
	init {
		// Set up URI paths
		uriMatcher.addURI(authority, Operation.ALL.table.toLowerCase(), Operation.ALL.ordinal)
		uriMatcher.addURI(authority, "${Operation.SINGLE.table.toLowerCase()}/#", Operation.SINGLE.ordinal)
	}
	
	/**
	 * Returns all the [db]'s [DBOpenHelper.TableColumn] from a [uri] using the [uriMatcher]
	 */
	private fun dbTable(uri: Uri): DBOpenHelper.Table {
		val match = uriMatcher.match(uri)
		val op = Operation.values()[match]
		return DBOpenHelper.Table.valueOf(op.table)
	}
	
	// Override of methods from ContentProvider class
	
	/**
	 * Lazy initialisation of [db] as recommended in documentation, so don't initialise here
	 */
	override fun onCreate(): Boolean = true
	
	/**
	 * Custom MIME type for this application
	 */
	override fun getType(uri: Uri): String? = "${BuildConfig.APPLICATION_ID}.item"
	
	override fun query(uri: Uri, projection: Array<String>?,
	                   selection: String?, selectionArgs: Array<String>?,
	                   sortOrder: String?): Cursor? {
		// Get table to perform query on
		val table = dbTable(uri)
		return db.query(table.name, projection, selection, selectionArgs, null, null, sortOrder)
	}
	
	/**
	 * Insert [values] into the [db] in the table determined by parsing the [uri]
	 *
	 * Returns [Uri] pointing to newly inserted values
	 */
	override fun insert(uri: Uri, values: ContentValues?): Uri? {
		// Get table to insert data into
		val table = dbTable(uri)
		// Perform insertion and get ID for row just inserted
		val insertionId = db.insert(table.name, null, values)
		// Convert ID into Uri to return
		val newUri = Uri.withAppendedPath(uri, insertionId.toString())
		// Notify change occurred
		context.contentResolver.notifyChange(uri, null)
		// Return Uri to the new row inserted
		return newUri
	}
	
	override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
		// Get table to perform update operation on
		val table = dbTable(uri)
		// Get number of rows which were updated to be returned
		val rows = db.update(table.name, values, selection, selectionArgs)
		// Notify change occurred
		if (rows > 0) context.contentResolver.notifyChange(uri, null)
		// Return number of rows updated
		return rows
	}
	
	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
		// Get table to perform delete operation on
		val table = dbTable(uri)
		// Tell database to delete matching rows
		val rows = db.delete(table.name, selection, selectionArgs)
		// Notify change occurred
		if (rows > 0) context.contentResolver.notifyChange(uri, null)
		// Return number of rows deleted
		return rows
	}
}
