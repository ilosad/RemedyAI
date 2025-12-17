package com.example.remedyai.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.remedyai.api.GPTService
import com.example.remedyai.api.FirestoreService
import com.example.remedyai.model.SurveyResult

@Composable
fun ResultScreen(
    symptom: String,
    duration: String,
    severity: String,
    navController: NavController
) {
    val context = LocalContext.current
    var aiResult by remember { mutableStateOf<GPTService.EmergencyAIResult?>(null) }
    var reportExpanded by remember { mutableStateOf(false) }

    // ✅ GPT 호출 + 실패 시 fallback(무한 로딩 방지)
    LaunchedEffect(symptom, duration, severity) {
        GPTService.getEmergencyAdvice(symptom, duration, severity) { result ->
            aiResult = result ?: GPTService.EmergencyAIResult(
                level = if (severity == "매우 심함") "응급" else "주의",
                summary = "증상 정보를 바탕으로 즉각적인 대응이 필요합니다.",
                action = listOf(
                    "즉시 안전한 장소에서 안정을 취하세요",
                    "혼자 있지 말고 주변 사람에게 상황을 알리세요",
                    "증상이 악화되면 지체 없이 119 또는 응급실로 이동하세요"
                ),
                warning = "혼자 판단하여 치료를 미루거나 이동을 지연하지 마세요.",
                call = severity == "매우 심함"
            )

            // ✅ 저장(기존 구조 유지)
            aiResult?.let {
                FirestoreService.saveSurveyResult(
                    SurveyResult(
                        symptom = symptom,
                        duration = duration,
                        severity = severity,
                        level = it.level,
                        aiAdvice = it.summary
                    ),
                    onSuccess = {},
                    onFailure = {}
                )
            }
        }
    }

    // 로딩 중(그래도 화면은 의미 있게)
    if (aiResult == null) {
        LoadingEmergencyScreen(severity)
        return
    }

    val result = aiResult!!

    // 🔥 2단계 업그레이드 #1: 위험도 시각화(게이지)
    val riskScore = when (result.level) {
        "응급" -> 0.90f
        "주의" -> 0.60f
        else -> 0.30f
    }

    val levelColor = when (result.level) {
        "응급" -> Color(0xFFB71C1C)
        "주의" -> Color(0xFFF57C00)
        else -> Color(0xFF2E7D32)
    }

    val riskLabel = when (result.level) {
        "응급" -> "높음"
        "주의" -> "중간"
        else -> "낮음"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ✅ 상단 요약 카드
        Card(
            colors = CardDefaults.cardColors(containerColor = levelColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "응급 등급: ${result.level}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = result.summary,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 🔥 위험도 게이지(2단계 핵심)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("AI 위험도 시각화", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("위험도: $riskLabel", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { riskScore },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = levelColor,
                    trackColor = Color(0xFFE5E7EB)
                )
                Spacer(Modifier.height(10.dp))

                // 입력 요약(하단 공백 줄이고 “리포트 느낌” 강화)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("증상: $symptom", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.MedicalServices, null) }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("기간: $duration", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Schedule, null) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("강도: $severity", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.WarningAmber, null) }
                    )
                }
            }
        }

        // ✅ 즉시 행동 가이드
        Text("지금 즉시 해야 할 행동", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // 행동 리스트(기존 + 안정성/전문성용 1~2개 보강)
        val actions = result.action +
                listOf(
                    "증상이 급격히 악화되면 즉시 119 또는 응급실로 이동하세요",
                    "가능하면 주변 사람에게 현재 상태를 공유하고 혼자 있지 마세요"
                )

        actions.distinct().take(5).forEach {
            ActionRow(Icons.Default.CheckCircle, it)
        }

        // ⚠️ 주의 사항 카드
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(result.warning, fontSize = 14.sp)
            }
        }

        // ✅ CTA 버튼 영역
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = levelColor),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("119에 전화하기", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = { navController.navigate("hospital") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("가까운 병원 안내 보기")
        }

        // 🔥 2단계 업그레이드 #2: AI 판독 리포트(토글)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("AI 판독 리포트", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { reportExpanded = !reportExpanded }) {
                        Text(if (reportExpanded) "접기" else "자세히 보기")
                    }
                }

                // 기본(항상 보이는) 한 줄
                Text(
                    "AI는 입력값(증상/기간/강도)을 종합해 위험도를 추정하고, 즉시 행동을 추천합니다.",
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )

                if (reportExpanded) {
                    Spacer(Modifier.height(10.dp))

                    Divider()

                    Spacer(Modifier.height(10.dp))
                    Text("판독 근거 요약", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))

                    ActionRow(Icons.Default.FactCheck, "증상: \"$symptom\"")
                    ActionRow(Icons.Default.Timelapse, "지속: \"$duration\"")
                    ActionRow(Icons.Default.PriorityHigh, "강도: \"$severity\"")
                    ActionRow(Icons.Default.Rule, "위험도: $riskLabel (등급: ${result.level})")

                    Spacer(Modifier.height(10.dp))
                    Text("해석 가이드", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• 이 결과는 응급 대응을 돕기 위한 참고 정보입니다.\n" +
                                "• 증상이 심해지거나 신경학적 이상(의식 저하, 마비 등)이 동반되면 즉시 119 또는 응급실을 권장합니다.\n" +
                                "• 최종 판단은 의료진의 평가가 우선입니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )
                }
            }
        }

        // ✅ 하단 고지(“AI가 썼다”가 명확히 남음)
        Text(
            "※ 본 결과는 AI 분석을 기반으로 제공되며, 최종 의료 판단은 전문 의료진에 의해 이루어져야 합니다.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))
    }
}

/* ---------------- 보조 Composables ---------------- */

@Composable
private fun LoadingEmergencyScreen(severity: String) {
    val color = when (severity) {
        "매우 심함" -> Color(0xFFB71C1C)
        "중간" -> Color(0xFFF57C00)
        else -> Color(0xFF2E7D32)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "응급 상태 분석 중",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "AI가 증상 정보를 분석하고 있습니다.",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

        CircularProgressIndicator()

        Text(
            text = "위급하다고 느껴지면 즉시 119에 전화하세요.",
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 15.sp)
    }
}
