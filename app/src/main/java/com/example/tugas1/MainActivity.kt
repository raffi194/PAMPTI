package com.example.tugas1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.tugas1.ui.nav.AppBottomNavigation
import com.example.tugas1.ui.pages.*
import com.example.tugas1.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    // ✅ SATU ViewModel CHAT untuk semua halaman
    val chatViewModel: ChatViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ✅ Bottom nav hanya tampil di halaman utama
    val showBottomBar = currentRoute in listOf(
        "dashboard",
        "chat"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("dashboard") {
                DashboardScreen(navController)
            }

            composable("chat") {
                ChatScreen(
                    navController = navController,
                    chatViewModel = chatViewModel
                )
            }

            // ❗ Chat detail TANPA bottom nav
            composable("chat_detail") {
                ChatDetailScreen(
                    navController = navController,
                    chatViewModel = chatViewModel
                )
            }
        }
    }
}
