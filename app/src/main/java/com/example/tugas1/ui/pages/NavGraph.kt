package com.example.tugas1.ui.pages

// Contoh di dalam file NavGraph.kt atau di mana pun Anda mendefinisikan NavHost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tugas1.ui.pages.DashboardScreen
import com.example.tugas1.ui.pages.ProductDetailScreen // <- Import halaman baru

// ...

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        // Halaman Dashboard
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }

        // --- INI BAGIAN PENTINGNYA ---
        // Definisikan route baru untuk detail produk dengan argumen 'productId'
        composable(
            route = "product_detail/{productId}", // Nama route dan argumen
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Ambil argumen productId dari route
            val productId = backStackEntry.arguments?.getString("productId")

            // Tampilkan halaman detail dengan ID yang didapat
            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }

        // composable("cart") { ... }
        // composable("profile") { ... }
        // ... (route lainnya)
    }
}