package com.example.remedyai.api

import com.example.remedyai.BuildConfig
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object GPTService {

    private const val BASE_URL = "https://api.openai.com/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // 🔹 GPT 메시지 구조
    data class ChatMessage(val role: String, val content: String)
    data class ChatRequest(val model: String, val messages: List<ChatMessage>)
    data class ChatChoice(val message: ChatMessage)
    data class ChatResponse(val choices: List<ChatChoice>?)

    // 🔹 응급 판단 결과 (UI에서 바로 사용)
    data class EmergencyAIResult(
        val level: String,          // "응급" | "주의" | "안정"
        val summary: String,        // 한 줄 요약
        val action: List<String>,   // 지금 해야 할 행동
        val warning: String,        // 하면 안 되는 행동
        val call: Boolean           // 119 필요 여부
    )

    fun getEmergencyAdvice(
        symptom: String,
        duration: String,
        severity: String,
        callback: (EmergencyAIResult?) -> Unit
    ) {
        val apiKey = BuildConfig.OPENAI_API_KEY

        val prompt = """
사용자는 '${symptom}' 증상을 '${duration}' 동안 겪고 있으며,
통증의 정도는 '${severity}'입니다.

아래 JSON 형식으로만 응답하세요.
응급 상황에서 즉시 판단 가능해야 하며 문장은 짧아야 합니다.

{
  "level": "응급 | 주의 | 안정",
  "summary": "한 문장 요약",
  "action": [
    "지금 즉시 해야 할 행동 1",
    "지금 즉시 해야 할 행동 2"
  ],
  "warning": "절대 하면 안 되는 행동",
  "call": true | false
}
""".trimIndent()

        val requestBody = ChatRequest(
            model = "gpt-4.1-mini",
            messages = listOf(
                ChatMessage("system", "당신은 응급의학 전문 의료 상담 AI입니다."),
                ChatMessage("user", prompt)
            )
        )

        val body = gson.toJson(requestBody)
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    callback(null)
                    return
                }

                try {
                    val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
                    val content = chatResponse
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content

                    val result = gson.fromJson(content, EmergencyAIResult::class.java)
                    callback(result)

                } catch (e: Exception) {
                    callback(null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
        })
    }
}
