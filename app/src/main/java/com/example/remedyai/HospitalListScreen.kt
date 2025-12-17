package com.example.remedyai.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.remedyai.model.Hospital
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalListScreen(navController: NavController) {
    val context = LocalContext.current

    val hospitalList = listOf(
        Hospital("허리편한병원 (정형외과)", LatLng(36.994484, 127.142371), "031-656-2110"),
        Hospital("평택대학교 보건진료소", LatLng(36.994500, 127.142400), "031-659-8119"),
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
            TopAppBar(
                title = { Text("병원 목록", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            items(hospitalList) { hospital ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${hospital.phone}")
                            context.startActivity(intent)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = hospital.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "전화번호: ${hospital.phone}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
