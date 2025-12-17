package com.example.remedyai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/* ---------------- 데이터 모델 ---------------- */

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>
)

/*
 * ✔ 핵심 질문 3개 + 보조 질문 2개
 * ✔ 보조 질문은 응급 위험 신호 판단용 (UI/설득력 강화)
 */
val questionList = listOf(
    Question(1, "어디가 불편하신가요?", listOf("두통", "복통", "호흡곤란", "출혈")),
    Question(2, "증상이 얼마나 지속됐나요?", listOf("1시간 미만", "1~3시간", "하루 이상")),
    Question(3, "통증의 강도는 어떤가요?", listOf("약함", "중간", "매우 심함")),
    Question(4, "의식이 흐려지거나 어지러움을 느낀 적이 있나요?", listOf("없음", "조금 있음", "자주 있음")),
    Question(5, "증상이 갑자기 시작되었나요?", listOf("아니요", "예"))
)

/* ---------------- Survey Screen ---------------- */

@Composable
fun SurveyScreen(navController: NavController) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // 핵심 결과값 (ResultScreen으로 전달)
    var selectedSymptom by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf<String?>(null) }
    var selectedSeverity by remember { mutableStateOf<String?>(null) }

    // 보조 질문 저장 (지금은 직접 사용 안 함 → 확장 대비)
    var extraAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }

    var showResult by remember { mutableStateOf(false) }

    /* ---------------- 결과 화면 ---------------- */
    if (showResult) {
        ResultScreen(
            symptom = selectedSymptom ?: "",
            duration = selectedDuration ?: "",
            severity = selectedSeverity ?: "",
            navController = navController
        )
        return
    }

    val question = questionList[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1).toFloat() / questionList.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        /* ---------- 상단: AI 문진 상태 ---------- */

        Text(
            text = "AI가 환자 정보를 분석하고 있습니다",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        /* ---------- 중앙: 질문 영역 (가운데 정렬 핵심) ---------- */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // ⭐ 화면 중앙 정렬의 핵심
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = question.text,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 전문가 느낌용 힌트 문구
            Text(
                text = "응급 상황 판단을 위한 중요한 질문입니다",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            question.options.forEach { option ->
                Button(
                    onClick = { selectedOption = option },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedOption == option)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = option, fontSize = 16.sp)
                }
            }
        }

        /* ---------- 하단: 다음 / 진행도 ---------- */

        Button(
            onClick = {
                when (currentQuestionIndex) {
                    0 -> selectedSymptom = selectedOption
                    1 -> selectedDuration = selectedOption
                    2 -> selectedSeverity = selectedOption
                    else -> {
                        selectedOption?.let { extraAnswers[question.id] = it }
                    }
                }

                if (currentQuestionIndex < questionList.size - 1) {
                    currentQuestionIndex++
                    selectedOption = null
                } else {
                    showResult = true
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (currentQuestionIndex == questionList.size - 1)
                    "AI 판독 시작"
                else
                    "다음",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "문진 ${currentQuestionIndex + 1} / ${questionList.size}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
