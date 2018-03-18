package com.georgegarside.cryptovalise.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBOpenHelper(context: Context) : SQLiteOpenHelper(context, "coins.db", null, 1) {
	class Table(val name: String, vararg val columns: String)
	
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
		tables.forEach {
			db.execSQL((
					"CREATE TABLE ${it.name} (" +
							"${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
							it.columns.joinToString(" TEXT, ")
					).trim(' ', ',') + ")")
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
