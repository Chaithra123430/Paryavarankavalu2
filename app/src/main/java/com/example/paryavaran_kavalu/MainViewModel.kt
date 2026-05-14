package com.example.paryavaran_kavalu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    val reports: StateFlow<List<WasteReport>> = repository.reports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ecoKarmaPoints: StateFlow<Int> = repository.ecoKarmaPoints
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addReport(location: LatLng, wasteType: String, photoPath: String?) {
        viewModelScope.launch {
            val newReport = WasteReport(
                latitude = location.latitude,
                longitude = location.longitude,
                wasteType = wasteType,
                photoPath = photoPath
            )
            repository.addReport(newReport)
        }
    }

    fun markAsCleaned(report: WasteReport) {
        viewModelScope.launch {
            repository.updateReport(report.copy(status = ReportStatus.CLEANED))
        }
    }
}
