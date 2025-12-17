package com.example.remedyai.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await
import com.example.remedyai.model.Hospital
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalScreen(navController: NavController) {
    val context = LocalContext.current

    // 위치 권한 상태 체크
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // 권한 요청
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    // 내 위치로 카메라 이동 (권한 있으면)
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
            } catch (e: Exception) {
                null
            }

            val defaultLocation = LatLng(37.5665, 126.9780) // 서울 시청 fallback
            val finalLatLng = location?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(finalLatLng, 16f)
            )
        }
    }

    // 병원 데이터 리스트
    val hospitalList = listOf(
        Hospital("허리편한병원 (정형외과)", LatLng(36.994484, 127.142371), "031-656-2110"),
        Hospital("평택대학교 보건진료소", LatLng(36.995965, 127.133423), "031-659-8119"),
        Hospital("평택서울정형외과의원 (정형외과)", LatLng(36.990000, 127.110000), "031-655-1234"),
        Hospital("연세우리내과의원 (내과)", LatLng(36.991500, 127.113000), "031-658-5678"),
        Hospital("한빛치과의원 (치과)", LatLng(36.990500, 127.111500), "031-651-2345"),
        Hospital("삼성안과의원 (안과)", LatLng(36.991800, 127.114000), "031-653-3456"),
        Hospital("평택이비인후과의원 (이비인후과)", LatLng(36.990800, 127.112000), "031-654-4567"),
        Hospital("서울산부인과의원 (산부인과)", LatLng(36.992200, 127.115500), "031-652-5678"),
        Hospital("평택한의원 (한의원)", LatLng(36.991000, 127.113000), "031-655-6789"),
        Hospital("굿모닝병원 (종합병원)", LatLng(36.992500, 127.112500), "031-659-7200"),
        Hospital("굿모닝병원 응급의료센터", LatLng(36.990818, 127.120396), "031-659-7200")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("내 주변 병원 안내") })
        },
        bottomBar = {
            BottomAppBar {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    navController.navigate("hospital_list")
                }) {
                    Text("병원 목록 보기")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { innerPadding ->
        if (hasPermission) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                hospitalList.forEach { hospital ->
                    Marker(
                        state = MarkerState(position = hospital.location),
                        title = hospital.name,
                        snippet = "전화번호: ${hospital.phone}"
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("위치 권한이 필요합니다.")
            }
        }
    }
}