package com.example.tugas1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tugas1.ui.LoginScreen
import com.example.tugas1.ui.RegisterScreen
import com.example.tugas1.ui.pages.*
import com.example.tugas1.viewmodel.AuthViewModel
import com.example.tugas1.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Cukup panggil MyApp, semua logika ada di dalamnya
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val productViewModel: ProductViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Pantau status login dari ViewModel
    val isAuthenticated by authViewModel.authState.collectAsState()

    MaterialTheme {
        // NavHost utama yang akan beralih antara dua grafik navigasi
        NavHost(
            navController = navController,
            // Tentukan tujuan awal berdasarkan status login saat aplikasi pertama kali dibuka
            startDestination = if (isAuthenticated) "main_graph" else "auth_graph"
        ) {
            // --- Grafik 1: Alur Autentikasi (jika belum login) ---
            navigation(
                startDestination = "login",
                route = "auth_graph"
            ) {
                composable("login") { LoginScreen(navController, authViewModel) }
                composable("register") { RegisterScreen(navController, authViewModel) }
            }

            // --- Grafik 2: Alur Aplikasi Utama (jika sudah login) ---
            navigation(
                startDestination = "dashboard",
                route = "main_graph"
            ) {
                composable("dashboard") { DashboardScreen(navController, productViewModel) }
                composable("cart") { CartScreen(navController, productViewModel) }
                composable("wishlist") { WishlistScreen(navController, productViewModel) }
                composable("profile") { ProfileScreen(navController, authViewModel) }
                composable("notification") { NotificationScreen(navController) }
                composable("chat") { ChatScreen(navController) }
                composable("checkout") { CheckoutScreen(navController, productViewModel) }

                composable(
                    route = "productDetail/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    ProductDetailScreen(
                        navController = navController,
                        productViewModel = productViewModel,
                        productId = productId
                    )
                }
            }
        }

        // --- PENGAWAS UTAMA: LaunchedEffect di luar NavHost ---
        // Tugasnya adalah memindahkan pengguna antar grafik saat status login berubah.
        LaunchedEffect(isAuthenticated) {
            if (isAuthenticated) {
                // Jika user berhasil login (isAuthenticated menjadi true),
                // navigasi ke grafik utama.
                navController.navigate("main_graph") {
                    // Hapus grafik autentikasi dari backstack agar tidak bisa kembali ke login.
                    popUpTo("auth_graph") { inclusive = true }
                }
            } else {
                // Jika user logout (isAuthenticated menjadi false),
                // navigasi kembali ke grafik autentikasi.
                // Pengecekan ini untuk menghindari navigasi berulang jika sudah berada di halaman login.
                if (navController.currentBackStackEntry?.destination?.parent?.route != "auth_graph") {
                    navController.navigate("auth_graph") {
                        // Hapus grafik utama dari backstack agar tidak bisa kembali ke dashboard.
                        popUpTo("main_graph") { inclusive = true }
                    }
                }
            }
        }
    }
}
