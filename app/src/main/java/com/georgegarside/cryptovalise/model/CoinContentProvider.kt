package com.georgegarside.cryptovalise.model

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.georgegarside.cryptovalise.BuildConfig

/**
 * CoinContentProvider is a [ContentProvider] of [Coin]. This content provider matches for any [Operation] and performs
 * the actions on the [db] utilising a [dbOpenHelper].
 */
class CoinContentProvider : ContentProvider() {
	
	companion object {
		/**
		 * The base [Uri] on which all [Operation] paths are based on. This adds the content scheme to create a base Uri.
		 * The authority is equal to the application package name. This constant is read from the BuildConfig to avoid the
		 * need to pass a context into the class constructor, necessary for getPackageName. Since the package name will not
		 * change, this is an additional performance benefit by using this build-time constant.
		 */
		val baseUri = Uri.parse("content://${BuildConfig.APPLICATION_ID}")!!
	}
	
	/**
	 * An instance of a [UriMatcher] for performing matching from a [Uri] to [Operation].
	 */
	private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
	/**
	 * A reference to the database open helper, which provides access to the database and enums for the tables contained.
	 */
	private val dbOpenHelper by lazy { DBOpenHelper(context) }
	/**
	 * A reference to the [SQLiteDatabase] which provides the storage for this content provider. The reference to the
	 * database is instantiated lazily, so the database file is not opened for reading or writing until a database action
	 * is requested form this content provider. This means simply instantiating this class does not create the database
	 * yet until a [query] of the content is requested or a mutation action is performed.
	 */
	private val db: SQLiteDatabase by lazy { dbOpenHelper.writableDatabase }
	
	/**
	 * Each operation which can be performed on the database, depending on the URI which was matched with [uriMatcher].
	 * For each operation, a [table] name is defined which may not match the name of the operation, and a [uri] which
	 * represents this operation being performed, such that this content provider can respond at this Uri by performing
	 * the operation.
	 */
	enum class Operation(val table: String, val uri: Uri) {
		Coin("Coin", Uri.withAppendedPath(baseUri, "coin"));
	}
	
	init {
		// Set up URI paths
		uriMatcher.addURI(BuildConfig.APPLICATION_ID, Operation.Coin.table.toLowerCase(), Operation.Coin.ordinal)
	}
	
	/**
	 * Returns the [DBOpenHelper.Table]
	 */
	private fun dbTable(uri: Uri): DBOpenHelper.Table {
		/**
		 * The ordinal of the matched operation from the Uri.
		 */
		val match = uriMatcher.match(uri)
		/**
		 * The operation to be performed.
		 */
		val op = Operation.values()[match]
		// Returns the table.
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
