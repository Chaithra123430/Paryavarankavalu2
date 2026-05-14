package com.example.paryavaran_kavalu.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paryavaran_kavalu.ImageUtils
import com.example.paryavaran_kavalu.MainViewModel
import com.example.paryavaran_kavalu.R
import com.example.paryavaran_kavalu.ReportStatus
import com.example.paryavaran_kavalu.WasteReport
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val points by viewModel.ecoKarmaPoints.collectAsStateWithLifecycle()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 14f)
    }

    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Auto-center camera on user's location at startup
    LaunchedEffect(Unit) {
        if (hasLocationPermission.value) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                        )
                    }
                }
        }
    }
    
    var showReportDialog by remember { mutableStateOf(false) }
    var reportToClean by remember { mutableStateOf<WasteReport?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoPath by remember { mutableStateOf<String?>(null) }
    
    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                scope.launch {
                    val compressedFile = ImageUtils.compressImage(context, uri)
                    if (compressedFile != null) {
                        currentPhotoPath = compressedFile.absolutePath
                        showReportDialog = true
                    } else {
                        Toast.makeText(context, "Image compression failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    getCurrentLocation(context) { latLng ->
                        selectedLatLng = latLng
                        val photoFile = createImageFile(context)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        tempPhotoUri = uri
                        takePhotoLauncher.launch(uri)
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_report)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission.value,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false
                )
            ) {
                reports.forEach { report ->
                    key(report.id) {
                        val isCleaned = report.status == ReportStatus.CLEANED
                        Marker(
                            state = rememberMarkerState(position = report.location),
                            title = "${report.wasteType} Waste",
                            snippet = if (isCleaned) "Status: Cleaned" else "Status: Pending",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (isCleaned) BitmapDescriptorFactory.HUE_GREEN 
                                else BitmapDescriptorFactory.HUE_RED
                            ),
                            onClick = {
                                if (!isCleaned) {
                                    reportToClean = report
                                }
                                false
                            }
                        )
                    }
                }
            }

            // Points Badge
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onNavigateToDashboard() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$points Points",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showReportDialog && selectedLatLng != null) {
            WasteTypeDialog(
                onDismiss = { showReportDialog = false },
                onConfirm = { type ->
                    viewModel.addReport(selectedLatLng!!, type, currentPhotoPath)
                    showReportDialog = false
                    Toast.makeText(context, context.getString(R.string.report_success), Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (reportToClean != null) {
            AlertDialog(
                onDismissRequest = { reportToClean = null },
                title = { Text(stringResource(R.string.clean_dialog_title)) },
                text = { Text(stringResource(R.string.clean_dialog_msg, reportToClean!!.wasteType)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markAsCleaned(reportToClean!!)
                            reportToClean = null
                            Toast.makeText(context, context.getString(R.string.clean_success), Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportToClean = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun WasteTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val types = listOf("Plastic", "Organic", "Electronic", "Hazardous", "Other")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_waste_type)) },
        text = {
            Column {
                types.forEach { type ->
                    ListItem(
                        headlineContent = { Text(type) },
                        modifier = Modifier.clickable { onConfirm(type) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(context: Context, onLocationReceived: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(LatLng(location.latitude, location.longitude))
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
