package com.georgegarside.cryptovalise.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import kotlin.reflect.KClass

class DBOpenHelper(context: Context) : SQLiteOpenHelper(context, "coins.db", null, 1) {
	
	enum class SQL(private val sql: String) {
		Table("CREATE TABLE"),
		Int("INTEGER"),
		Text("TEXT"),
		Key("PRIMARY KEY"),
		Auto("AUTOINCREMENT"),
		Unique("UNIQUE");
		
		override fun toString(): String = this.sql
	}
	
	interface TableColumn {
		val type: SQL
		val extras: Array<out SQL>
		val column: String
	}
	
	private val <T : TableColumn> KClass<T>.declaration
		get() = this.java.enumConstants.joinToString(
				prefix = "${SQL.Table} ${this.simpleName} (",
				separator = ", ",
				postfix = ")"
		) {
			return@joinToString "${it.column} ${it.type} ${it.extras.joinToString(" ")}"
		}
	
	enum class Coin(override val type: SQL, override vararg val extras: SQL) : TableColumn {
		/**
		 * ID column is an integer which is the primary key in table, marked as autoincrementing so it does not need to be
		 * provided when adding a new row to the table.
		 */
		ID(SQL.Int, SQL.Key, SQL.Auto) {
			/**
			 * ID column name must match [BaseColumns._ID] such that [android.provider] can access data correctly
			 */
			override val column = BaseColumns._ID
		},
		Symbol(SQL.Text, SQL.Unique),
		Name(SQL.Text);
		
		/**
		 * Open the column's [name] for overriding by a specific column declaration
		 */
		override val column = this.name
		
		companion object {
			val columns = values().map { it.column }.toTypedArray()
		}
	}
	
	enum class Table(val columns: Array<String>) {
		Coin(DBOpenHelper.Coin.columns);
	}
	
	/**
	 * Creates the [db] and populates it with [TableColumn]s defined in [Table]
	 */
	override fun onCreate(db: SQLiteDatabase) {
		// Need to create a table in the database for each defined table
		db.execSQL(Coin::class.declaration)
		
		// Insert starter content
		db.execSQL("""
			INSERT INTO ${Coin::class.simpleName} ("${Coin.Symbol}", "${Coin.Name}")
			VALUES ("BTC", "Bitcoin"), ("ETH", "Ethereum")
		""".trimIndent())
	}
	
	/**
	 * Upgrades the [db] from [oldVersion] to [newVersion] by dropping necessary tables and running [onCreate]
	 */
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// Drop all tables in database
		db.execSQL("DROP TABLE IF EXISTS ${Coin::class.simpleName}")
		// Recreate database from scratch
		onCreate(db)
	}
}
