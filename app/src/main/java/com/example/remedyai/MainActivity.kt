package com.example.remedyai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.remedyai.ui.theme.RemedyAITheme
import com.example.remedyai.ui.HospitalListScreen
import com.example.remedyai.ui.HospitalScreen
import com.example.remedyai.ui.SurveyScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemedyAITheme {
                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}

// 🔷 네비게이션 그래프
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("survey") { SurveyScreen(navController) }
        composable("hospital") { HospitalScreen(navController) }
        composable("hospital_list") { HospitalListScreen(navController) }
    }
}

// 🔷 메인 화면
@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "REMEDY.AI",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("survey") }
        ) {
            Text("문진 시작하기")
        }
    }
}