package com.georgegarside.cryptovalise.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.widget.CursorAdapter

class CustomLoader(private val context: Context,
                   private val uri: Uri,
                   private val cursorAdapter: CursorAdapter) : LoaderManager.LoaderCallbacks<Cursor> {
	
	override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
		return CursorLoader(context, uri, null, null, null, null)
	}
	
	override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
		cursorAdapter.swapCursor(data)
	}
	
	override fun onLoaderReset(loader: Loader<Cursor>) {
		cursorAdapter.swapCursor(null)
	}
}
