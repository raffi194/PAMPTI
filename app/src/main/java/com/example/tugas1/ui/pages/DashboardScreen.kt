package com.example.tugas1.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tugas1.ui.nav.BottomNavItem
import com.example.tugas1.viewmodel.ProductViewModel

@Composable
fun DashboardScreen(navController: NavController, productViewModel: ProductViewModel) {

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "dashboard"),
        BottomNavItem("Cart", Icons.Default.ShoppingCart, "cart"),
        BottomNavItem("Wishlist", Icons.Default.Favorite, "wishlist"),
        BottomNavItem("Chat", Icons.Default.Chat, "chat"),
        BottomNavItem("Checkout", Icons.Default.Check, "checkout"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )


    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        DashboardContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun DashboardContent(modifier: Modifier = Modifier) {

    val categories = listOf(
        "Watches", "Bags", "Beauty", "Clothing",
        "Accessories", "Shoes", "Lifestyle", "More"
    )

    val recommended = listOf(
        "Tas Cantik",
        "Sepatu Sport"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("What are you looking for?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text("Best Seller Banner", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Categories", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Grid kategori
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                items(items = categories) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recommended Header
        item {
            Text("Recommended", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Recommended List
        items(count = recommended.size) { index: Int ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        recommended[index],
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
