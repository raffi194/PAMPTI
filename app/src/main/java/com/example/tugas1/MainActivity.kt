package com.example.tugas1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tugas1.ui.LoginScreen
import com.example.tugas1.ui.RegisterScreen
import com.example.tugas1.ui.nav.AppBottomNavigation
import com.example.tugas1.ui.pages.*
import com.example.tugas1.ui.viewmodel.ChatViewModel
import com.example.tugas1.viewmodel.AuthViewModel
import com.example.tugas1.viewmodel.ProductViewModel
import com.example.tugas1.viewmodel.ProfileViewModel

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

    // Inisialisasi semua ViewModel di level tertinggi
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()

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
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {

            // ===== GRUP AUTH =====
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("register") { RegisterScreen(navController, authViewModel) }

            // ===== GRUP MENU UTAMA =====
            composable("dashboard") {
                DashboardScreen(navController = navController, productViewModel = productViewModel)
            }
            composable("cart") {
                CartScreen(navController = navController, productViewModel = productViewModel)
            }
            composable("order_history") {
                OrderHistoryScreen(navController = navController, productViewModel = productViewModel)
            }
            composable("chat") {
                ChatScreen(navController, chatViewModel)
            }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }

                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }

            // ===== GRUP PROSES & DETAIL =====
            composable("create_product") {
                CreateProductScreen(navController = navController, productViewModel = productViewModel)
            }
            composable("checkout") {
                CheckoutScreen(navController = navController, productViewModel = productViewModel)
            }
            composable("order_success") {
                OrderSuccessScreen(navController)
            }
            composable("chat_detail") {
                ChatDetailScreen(navController = navController, chatViewModel = chatViewModel)
            }

            // ================================================================
            // === INI BAGIAN YANG HILANG & MENYEBABKAN CRASH ===
            // Mendaftarkan rute detail produk dengan argumen "productId"
            // ================================================================
            composable(
                route = "product_detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(
                    navController = navController,
                    productId = productId,
                    productViewModel = productViewModel
                )
            }
            // ================================================================

            // ===== GRUP REVIEW =====
            composable(
                route = "submit_review/{orderId}/{productId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                SubmitReviewScreen(
                    navController = navController,
                    orderId = orderId,
                    productId = productId
                )
            }
            composable(
                route = "review_result/{orderId}/{productId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                ReviewResultScreen(
                    navController = navController,
                    orderId = orderId,
                    productId = productId
                )
            }
        }
    }
}