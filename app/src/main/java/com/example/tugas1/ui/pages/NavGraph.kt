package com.example.tugas1.ui.pages


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tugas1.ui.pages.CreateProductScreen
import com.example.tugas1.ui.pages.DashboardScreen
import com.example.tugas1.ui.pages.ProductDetailScreen




@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {


        // Halaman Dashboard
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }


        // Halaman Detail Produk
        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }


        // Rute untuk halaman tambah produk
        composable("create_product") {
            CreateProductScreen(navController = navController)
        }


        // Contoh rute lain (biarkan sebagai komentar jika belum dibuat)
        // composable("cart") {
        //     CartScreen(navController = navController)
        // }
    }
}