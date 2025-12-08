package com.example.tugas1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.tugas1.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    // ViewModel yang digunakan seluruh aplikasi
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val isAuthenticated by authViewModel.authState.collectAsState()

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) "main_graph" else "auth_graph"
        ) {

            // --------- AUTH GRAPH ----------
            navigation(
                startDestination = "login",
                route = "auth_graph"
            ) {
                composable("login") { LoginScreen(navController, authViewModel) }
                composable("register") { RegisterScreen(navController, authViewModel) }
            }

            // --------- MAIN GRAPH ----------
            navigation(
                startDestination = "dashboard",
                route = "main_graph"
            ) {

                composable("dashboard") {
                    DashboardScreen(navController, productViewModel)
                }

                composable("cart") {
                    CartScreen(navController, productViewModel)
                }

                composable("wishlist") {
                    WishlistScreen(navController, productViewModel)
                }

                composable("profile") {
                    ProfileScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel
                    )
                }

                composable("editProfile") {
                    EditProfileScreen(
                        navController = navController,
                        profileViewModel = profileViewModel
                    )
                }

                composable("notification") {
                    NotificationScreen(navController)
                }

                composable("chat") {
                    ChatScreen(navController)
                }

                composable("checkout") {
                    CheckoutScreen(navController, productViewModel)
                }

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

        // --------- AUTO NAVIGATE WHEN LOGIN STATE CHANGES ----------
        LaunchedEffect(isAuthenticated) {
            if (isAuthenticated) {
                navController.navigate("main_graph") {
                    popUpTo("auth_graph") { inclusive = true }
                }
            } else {
                if (navController.currentBackStackEntry?.destination?.parent?.route != "auth_graph") {
                    navController.navigate("auth_graph") {
                        popUpTo("main_graph") { inclusive = true }
                    }
                }
            }
        }
    }
}
