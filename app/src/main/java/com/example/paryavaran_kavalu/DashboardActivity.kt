package com.example.paryavaran_kavalu

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvPoints: TextView = findViewById(R.id.tvDashboardPoints)
        val tvReportCount: TextView = findViewById(R.id.tvReportCount)
        val tvCleanedCount: TextView = findViewById(R.id.tvCleanedCount)
        val btnBack: Button = findViewById(R.id.btnBack)

        // Observe Eco-Karma Points from ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ecoKarmaPoints.collect { points ->
                    tvPoints.text = points.toString()
                }
            }
        }

        // Observe Reports and update stats
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reports.collect { reports ->
                    tvReportCount.text = getString(R.string.total_reports, reports.size)
                    val cleanedCount = reports.count { it.status == ReportStatus.CLEANED }
                    tvCleanedCount.text = getString(R.string.spots_cleaned, cleanedCount)
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
