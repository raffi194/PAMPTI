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

    val showBottomBar = currentRoute in listOf("dashboard", "order_history", "chat")

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

    NavHost(
        navController = navController,
        startDestination = "auth_graph",
        modifier = modifier
    ) {
        // --- Graph untuk Autentikasi (Login/Register) ---
        navigation(startDestination = "login", route = "auth_graph") {
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("register") { RegisterScreen(navController, authViewModel) }
        }

        // --- Graph Utama Aplikasi (Setelah Login) ---
        navigation(startDestination = "dashboard", route = "main_graph") {
            composable("dashboard") { DashboardScreen(navController, productViewModel) }
            composable("order_history") { OrderHistoryScreen(navController) }
            composable("chat") { /* TODO: Buat ChatScreen */ }
            composable("order_success") {
                OrderSuccessScreen(navController)
            }

            composable("profile") {
                ProfileScreen(
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
