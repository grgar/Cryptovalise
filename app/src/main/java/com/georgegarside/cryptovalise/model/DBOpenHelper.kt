package com.georgegarside.cryptovalise.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import kotlin.reflect.KClass

/**
 * Open helper for the [SQLiteDatabase]. A subclass of [SQLiteOpenHelper], this class defines the database that is used
 * in the app for storing data. Each table is an enum class, with each table implementing a custom interface for
 * managing columns in a table: [TableColumn]. The interface provides the ability to write a column as an SQL
 * declaration, using a custom enum class [SQL]. Each table is in the enum [Table] which allows it to be created using
 * the overridden method [SQLiteOpenHelper.onCreate].
 */
class DBOpenHelper(context: Context) : SQLiteOpenHelper(context, "coins.db", null, 1) {
	
	/**
	 * SQL operations and key words which can be performed in a SQL statement. This enumeration defines the exact SQL
	 * string necessary to signify the representation of the enum. See individual enum documentation for further details.
	 */
	enum class SQL(private val sql: String) {
		/**
		 * Create a new table.
		 */
		Table("CREATE TABLE"),
		/**
		 * Integer column type.
		 */
		Int("INTEGER"),
		/**
		 * Free text column type.
		 */
		Text("TEXT"),
		/**
		 * Defines this column as a primary key, or part of a composite key, for the table.
		 */
		Key("PRIMARY KEY"),
		/**
		 * Automatically increment this [Int] column such that the column contains [Unique] values.
		 */
		Auto("AUTOINCREMENT"),
		/**
		 * Defines this column as only containing unique values, i.e. no two values in the column are equal.
		 */
		Unique("UNIQUE");
		
		/**
		 * The string representation of the enum is the SQL string which represents the SQL operation or entity signified.
		 */
		override fun toString(): String = this.sql
	}
	
	/**
	 * A column in a table, defined by the type of data stored within the column and any other modifiers added to the SQL.
	 */
	interface TableColumn {
		/**
		 * The type of data stored within the column.
		 */
		val type: SQL
		/**
		 * Additional modifiers on the column. The out modifier on the generic permits the use of vararg in defining the
		 * array for the implementation of this interface, which is useful to assist development as it is not necessary
		 * for the developer to create a new array when manually defining the list of extras from the [SQL] enum.
		 */
		val extras: Array<out SQL>
	}
	
	/**
	 * Get the declaration from the column as a SQL string which could be executed by the [SQLiteDatabase]. This string
	 * is a [SQL.Table] (create table) operation which can be directly executed with [SQLiteDatabase.execSQL]. The
	 * declaration returned by this method contains each [TableColumn] in the [Table] with all the [TableColumn.extras].
	 */
	private val <T : TableColumn> KClass<T>.declaration
		get() = this.java.enumConstants.joinToString(
				// Define this as a create table operation, with the name of the table as the simple name.
				prefix = "${SQL.Table} ${this.simpleName} (",
				// Each column declaration in SQL is separated by a comma
				separator = ", ",
				// The end of the definitions of columns is closed with a closing parenthesis.
				postfix = ")"
		) {
			// Each table's name, type and extras are joined and appended to a line for that column declaration
			return@joinToString "$it ${it.type} ${it.extras.joinToString(" ")}"
		}
	
	/**
	 * The coin table in the database, for storing coins saved by the user. Each coin saved by the user in the app adds
	 * a row to this database, storing the coin's [Coin.ID], its [Coin.Symbol] and common [Coin.Name]. Each column defined
	 * by this table is an implementation of the custom [TableColumn] interface, ensuring a column defines its [type] and
	 * an optional list of [extras] for the create declaration.
	 */
	enum class Coin(override val type: SQL, override vararg val extras: SQL) : TableColumn {
		/**
		 * ID column is an integer which is the primary [SQL.Key] in table. This ID is provided from the [API] and is
		 * [SQL.Unique] across all coins to uniquely identify the coin with the API and across the app.
		 */
		ID(SQL.Int, SQL.Key, SQL.Unique) {
			/**
			 * ID column name must match the constant from [BaseColumns._ID] such that [android.provider] can access data
			 * correctly in this database without additional mapping. Content provider methods will throw if there is no
			 * suitable ID column found within a table.
			 */
			override fun toString(): String = BaseColumns._ID
		},
		/**
		 * The standard canonical symbol referring to the coin in the database. This is also unique for the coin and is used
		 * to refer to resources relating to the coin in supplementary API requests (unlike ID which is used for API calls
		 * for the coin data itself rather than associated resources). This symbol is usually either 3 or 4 characters.
		 */
		Symbol(SQL.Text, SQL.Unique),
		/**
		 * The name of the coin as presented to the user. This name is presented to the user regularly as the name of the
		 * coin and does not have any requirements in length, word splitting or character set.
		 */
		Name(SQL.Text);
		
		companion object {
			/**
			 * An array of all the table names. Note that the returned array of strings does not include [TableColumn.extras].
			 * If one requires the extras and the [TableColumn.type], use [declaration].
			 */
			val columns = values().map { it.toString() }.toTypedArray()
		}
	}
	
	/**
	 * All tables available in the database. Each table has an array of [columns] which define the table structure. Each
	 * table can be written as a SQL table creation [declaration].
	 */
	enum class Table(private val columns: Array<String>) {
		/**
		 * [DBOpenHelper.Coin] table in the database.
		 * @see DBOpenHelper.Coin
		 */
		Coin(DBOpenHelper.Coin.columns);
	}
	
	/**
	 * Creates the [db] and populates it by adding each [TableColumn] defined in [Table].
	 */
	override fun onCreate(db: SQLiteDatabase) {
		// Need to create a table in the database for each defined table
		db.execSQL(Coin::class.declaration)
		
		// Insert starter content
		addSampleContent(db)
	}
	
	/**
	 * Initialise the database with sample content to get the user started with the app. When the user first launches the
	 * app and the [onCreate] is called to create the database, the table creation declarations create empty tables with
	 * no content and the user must add their own content to use the app. By starting the app with some sample rows in the
	 * database, the user can immediately begin using the app. Extra sample content can be removed from the database by
	 * the user if they do not wish to utilise the sample coins added, or they can continue adding their content as other
	 * rows — this does not affect standard operation of the app, but is a help to getting started with using the app.
	 */
	private fun addSampleContent(db: SQLiteDatabase) {
		// Add coins to the database
		db.execSQL("""
				INSERT INTO ${Coin::class.simpleName} ("${Coin.ID}", "${Coin.Symbol}", "${Coin.Name}")
				VALUES (236, "BTC", "Bitcoin"), (237, "ETH", "Ethereum")
			""".trimIndent())
	}
	
	/**
	 * Upgrades the [db] from [oldVersion] to [newVersion] by dropping necessary tables and running [onCreate].
	 * Since there has only ever been one version of the database, there is no database version-specific upgrade code.
	 * This method should not be called unless a database metadata error has occurred, therefore the ‘recovery’ to be
	 * executed within this method is to empty the database of all tables added by this open helper, then re-create the
	 * tables using the [onCreate] method also in this open helper.
	 */
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// Drop all tables in database
		db.execSQL("DROP TABLE IF EXISTS ${Coin::class.simpleName}")
		// Recreate database from scratch
		onCreate(db)
	}
}
