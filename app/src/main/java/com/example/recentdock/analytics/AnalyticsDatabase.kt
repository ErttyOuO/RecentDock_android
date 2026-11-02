package com.example.recentdock.analytics

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PackageCount(val packageName: String, val count: Int)

class AnalyticsDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usage_launch_event (
                package_name TEXT NOT NULL,
                event_time INTEGER NOT NULL,
                PRIMARY KEY(package_name, event_time)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE dock_launch_event (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                event_time INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_usage_time ON usage_launch_event(event_time)")
        db.execSQL("CREATE INDEX idx_dock_time ON dock_launch_event(event_time)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun insertUsageLaunch(packageName: String, eventTime: Long) {
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("event_time", eventTime)
        }
        writableDatabase.insertWithOnConflict(
            "usage_launch_event",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE,
        )
    }

    fun insertDockLaunch(packageName: String, eventTime: Long, source: String) {
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("event_time", eventTime)
            put("source", source)
        }
        writableDatabase.insert("dock_launch_event", null, values)
    }

    fun usageRanking(startInclusive: Long, endExclusive: Long): List<PackageCount> =
        ranking("usage_launch_event", startInclusive, endExclusive)

    fun dockRanking(startInclusive: Long, endExclusive: Long): List<PackageCount> =
        ranking("dock_launch_event", startInclusive, endExclusive)

    fun usageCount(startInclusive: Long, endExclusive: Long): Int =
        count("usage_launch_event", startInclusive, endExclusive)

    fun dockCount(startInclusive: Long, endExclusive: Long): Int =
        count("dock_launch_event", startInclusive, endExclusive)

    fun clearAllEvents() {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("usage_launch_event", null, null)
            writableDatabase.delete("dock_launch_event", null, null)
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun dockLaunchCountForPackage(packageName: String): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM dock_launch_event WHERE package_name = ?",
            arrayOf(packageName),
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    fun pruneEventsBefore(cutoff: Long) {
        writableDatabase.delete("usage_launch_event", "event_time < ?", arrayOf(cutoff.toString()))
        writableDatabase.delete("dock_launch_event", "event_time < ?", arrayOf(cutoff.toString()))
    }

    private fun count(table: String, startInclusive: Long, endExclusive: Long): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $table WHERE event_time >= ? AND event_time < ?",
            arrayOf(startInclusive.toString(), endExclusive.toString()),
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    private fun ranking(table: String, startInclusive: Long, endExclusive: Long): List<PackageCount> {
        val result = mutableListOf<PackageCount>()
        readableDatabase.rawQuery(
            """
            SELECT package_name, COUNT(*) AS total
            FROM $table
            WHERE event_time >= ? AND event_time < ?
            GROUP BY package_name
            ORDER BY total DESC, package_name ASC
            """.trimIndent(),
            arrayOf(startInclusive.toString(), endExclusive.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += PackageCount(cursor.getString(0), cursor.getInt(1))
            }
        }
        return result
    }

    companion object {
        private const val DATABASE_NAME = "recentdock_analytics.db"
        private const val DATABASE_VERSION = 1

        @Volatile private var instance: AnalyticsDatabase? = null

        fun getInstance(context: Context): AnalyticsDatabase =
            instance ?: synchronized(this) {
                instance ?: AnalyticsDatabase(context.applicationContext).also { instance = it }
            }
    }
}
