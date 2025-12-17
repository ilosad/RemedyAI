package com.example.remedyai.api

import com.example.remedyai.model.SurveyResult
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    fun saveSurveyResult(
        result: SurveyResult,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("survey_results")
            .add(result)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "저장 실패") }
    }
}
