package com.ianhanniballake.contractiontimer.data

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.provider.BaseColumns
import android.widget.AdapterView
import com.github.mikephil.charting.data.Entry
import com.ianhanniballake.contractiontimer.R
import com.ianhanniballake.contractiontimer.provider.ContractionContract
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.DateFormat
import java.util.ArrayList
import java.util.Date

/**
 * Handles converting Contractions to Entities for visualization
 */
object EntryTransformer {

    fun getAllContractionsAsEntries(context: Context, outputStream: OutputStream): ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        context.contentResolver.query(ContractionContract.Contractions.CONTENT_URI,
                null, null, null, null)?.use { data ->
            while (data.moveToNext()) {
                val startTimeColumnIndex = data.getColumnIndex(
                        ContractionContract.Contractions.COLUMN_NAME_START_TIME)
                val endTimeColumnIndex = data.getColumnIndex(
                        ContractionContract.Contractions.COLUMN_NAME_END_TIME)
                if (!data.isNull(endTimeColumnIndex) && !data.isNull(startTimeColumnIndex)) {
                    val startTime = data.getLong(startTimeColumnIndex)
                    val endTime = data.getLong(endTimeColumnIndex)
                    val entry = Entry(startTime.toFloat(), endTime.toFloat())
                    entries.add(entry)
                }

            }
        }
        return entries
    }
}
