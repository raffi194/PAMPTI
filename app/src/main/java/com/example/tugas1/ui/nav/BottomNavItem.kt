package com.example.tugas1.ui.nav

import androidx.compose.material.icons.Icons
// DITAMBAHKAN: Import ikon yang relevan
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong // Ikon baru untuk Riwayat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Data class ini tidak perlu diubah, sudah benar.
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppBottomNavigation(navController: NavController) {
    // DIUBAH: Daftar item navigasi diperbarui.
    val bottomNavItems = listOf(
        BottomNavItem(
            title = "Home",
            icon = Icons.Default.Home,
            route = "dashboard"
        ),
        BottomNavItem(
            title = "Riwayat", // Ganti label dari "Wishlist"
            icon = Icons.Default.ReceiptLong, // Ganti ikon
            route = "order_history" // Ganti route
        ),
        BottomNavItem(
            title = "Chat",
            icon = Icons.Default.Chat,
            route = "chat"
        ),
        BottomNavItem(
            title = "Profile",
            icon = Icons.Default.Person,
            route = "profile"
        )
        // Item "Checkout" dihapus karena biasanya tidak ada di navigasi utama bawah.
    )

    // State untuk mengetahui route yang sedang aktif (tidak diubah)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Logika ini sudah bagus, mencegah tumpukan navigasi yang besar.
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}
