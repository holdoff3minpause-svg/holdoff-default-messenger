package com.holdoff.app.sms

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

enum class DraftState { HELD, READY_TO_SEND, SENT, DISCARDED, FAILED }

data class HeldDraft(val id: Long, val address: String, val body: String, val state: DraftState)

/** Local-only draft ledger. Raw text never leaves the device unless the person opts into analysis. */
class DraftStore(context: Context) : SQLiteOpenHelper(context, "holdoff_drafts.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE drafts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            address TEXT NOT NULL,
            body TEXT NOT NULL,
            state TEXT NOT NULL,
            created_at INTEGER NOT NULL
        )""".trimIndent())
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun hold(address: String, body: String): Long = writableDatabase.insert("drafts", null, ContentValues().apply {
        put("address", address); put("body", body); put("state", DraftState.HELD.name)
        put("created_at", System.currentTimeMillis())
    })

    fun updateState(id: Long, state: DraftState) {
        writableDatabase.update("drafts", ContentValues().apply { put("state", state.name) }, "id=?", arrayOf(id.toString()))
    }
}
