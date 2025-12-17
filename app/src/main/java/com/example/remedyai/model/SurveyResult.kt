package com.example.remedyai.model

data class SurveyResult(
    val symptom: String = "",
    val duration: String = "",
    val severity: String = "",
    val level: String = "",             // 별점 예: ★★★★
    val aiAdvice: String = "",          // GPT 응답
    val timestamp: Long = System.currentTimeMillis() // 저장 시간
)
