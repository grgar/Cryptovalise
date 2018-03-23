package com.georgegarside.cryptovalise.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBOpenHelper(private val context: Context) : SQLiteOpenHelper(context, "coins.db", null, 1) {
	class Table(val name: String, vararg columns: String) {
		val columns = arrayOf(BaseColumns._ID, *columns)
	}
	
	companion object {
		val tables = arrayOf(Table("coin", "name", "symbol"))
		
		/**
		 * Get a [Table] by its [name]
		 */
		fun findTable(name: String): Table? {
			return tables.find { it.name == name }
		}
	}
	
	/**
	 * Creates the [db] and populates it with [Table]s defined in [tables]
	 */
	override fun onCreate(db: SQLiteDatabase) {
		// Need to create a table in the database for each defined table
		tables.forEach {
			db.execSQL((
					// Build SQL statement for creating table
					"CREATE TABLE ${it.name} (" +
							
							// ID column is primary key in table
							"${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
							
							// All other columns in table
							it.columns
									// Remove ID column since it was handled separately
									.drop(1)
									// All other columns are text
									.joinToString(" TEXT, ")
					
					)
					// Remove extraneous characters added by columns join
					.trim(' ', ',')
					// End SQL table definitions
					+ ")")
		}
		
		// Insert starter content
		findTable("coin")?.let {
			db.execSQL("""
				INSERT INTO ${it.name} ('name', 'symbol')
				VALUES ('Bitcoin', 'BTC'), ('Ethereum', 'ETH')
				""")
		}
	}
	
	/**
	 * Upgrades the [db] from [oldVersion] to [newVersion] by dropping necessary tables and running [onCreate]
	 */
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// Drop all tables in database
		tables.forEach {
			db.execSQL("DROP TABLE IF EXISTS ${it.name}")
		}
		// Recreate database from scratch
		onCreate(db)
	}
}
