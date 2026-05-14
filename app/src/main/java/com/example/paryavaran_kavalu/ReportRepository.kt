package com.example.paryavaran_kavalu

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    val reports: Flow<List<WasteReport>> = reportDao.getAllReports()
    val ecoKarmaPoints: Flow<Int?> = reportDao.getPoints()

    suspend fun addReport(report: WasteReport) {
        reportDao.insertReport(report)
    }

    suspend fun markAsCleaned(reportId: String) {
        // In a real app, we'd fetch the report first, but for simulation:
        // We'll update the status directly if we had a query, or just update the object.
        // For simplicity with the current DAO:
        // We assume the caller provides the updated report or we fetch it.
    }

    suspend fun updateReport(report: WasteReport) {
        reportDao.updateReport(report)
    }
}
