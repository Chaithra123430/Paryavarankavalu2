package com.example.paryavaran_kavalu

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "waste_reports")
data class WasteReport(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val wasteType: String,
    val status: ReportStatus = ReportStatus.REPORTED,
    val photoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val location: LatLng
        get() = LatLng(latitude, longitude)
}

enum class ReportStatus {
    REPORTED, CLEANED
}
