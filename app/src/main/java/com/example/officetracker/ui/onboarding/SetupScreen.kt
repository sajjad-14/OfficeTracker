package com.example.officetracker.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.officetracker.data.prefs.UserPreferences
import com.example.officetracker.worker.GeofenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val geofenceManager: GeofenceManager
) : ViewModel() {
    
    fun setOfficeLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            userPreferences.setOfficeLocation(lat, lng)
            geofenceManager.addGeofence(com.example.officetracker.data.prefs.OfficeLocation(lat, lng, isSet = true))
        }
    }
}

@Composable
fun SetupScreen(onSetupComplete: () -> Unit, viewModel: SetupViewModel = hiltViewModel()) {
    val context = LocalContext.current
    
    var hasForegroundPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var hasBackgroundPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Background permission implicitly granted < Q
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasForegroundPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             hasBackgroundPermission = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        }
    }

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Setup Office Location", 
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!hasForegroundPermission) {
            Text(
                "To track your attendance, we need to know when you are in the office. Please grant location access.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                launcher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )) 
            }) {
               Text("Grant Location Access") 
            }
        } else if (!hasBackgroundPermission) {
            Text(
                "For automatic entry/exit tracking while the app is closed, you MUST select 'Allow all the time' in the next screen.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }
            }) {
               Text("Allow All The Time") 
            }
        } else {
            Text(
                "You are ready! Stand at your desk and click below.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator()
            } else {
                Button(onClick = {
                    isLoading = true
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    try {
                        val cancellationTokenSource = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnSuccessListener { location ->
                            if (location != null) {
                                viewModel.setOfficeLocation(location.latitude, location.longitude)
                                onSetupComplete()
                            } else {
                                isLoading = false
                                // Could show a toast here "Unable to get location"
                            }
                        }.addOnFailureListener {
                            isLoading = false
                        }
                    } catch (e: SecurityException) {
                        isLoading = false
                    }
                }) {
                    Text("Set Current Location")
                }
            }
        }
    }
}
