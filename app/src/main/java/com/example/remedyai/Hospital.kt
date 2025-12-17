package com.example.remedyai.model

import com.google.android.gms.maps.model.LatLng

data class Hospital(
    val name: String,
    val location: LatLng,
    val phone: String,
          // 전문 과목 추가 (예: 정형외과, 내과)
)