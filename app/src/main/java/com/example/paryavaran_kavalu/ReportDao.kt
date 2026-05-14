package com.example.paryavaran_kavalu

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM waste_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WasteReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WasteReport)

    @Update
    suspend fun updateReport(report: WasteReport)

    @Query("SELECT SUM(CASE WHEN status = 'CLEANED' THEN 5 ELSE 10 END) FROM waste_reports")
    fun getPoints(): Flow<Int?>
}
