package com.example.paryavaran_kavalu

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WasteReport::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}
