package com.example.tugas1.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.ui.nav.BottomNavItem
import com.example.tugas1.viewmodel.ProductViewModel
import com.example.tugas1.R

// Data class untuk merepresentasikan produk
data class Product(
    val name: String,
    val category: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, productViewModel: ProductViewModel) {

    // Daftar item untuk bottom navigation tidak perlu diubah.
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "dashboard"),
        BottomNavItem("Cart", Icons.Default.ShoppingCart, "cart"),
        BottomNavItem("Wishlist", Icons.Default.Favorite, "wishlist"),
        // --- TAMBAHKAN KEMBALI DUA BARIS INI ---
        BottomNavItem("Chat", Icons.Default.Chat, "chat"),
        BottomNavItem("Checkout", Icons.Default.Check, "checkout"),
        // ------------------------------------
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    // State untuk mengetahui layar mana yang sedang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        // Kita tidak memakai TopAppBar di desain baru
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        // `selected` sekarang dinamis, akan menyorot ikon yang aktif
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Konten utama sekarang adalah LazyColumn yang berisi semua elemen
        DashboardContent(
            modifier = Modifier.padding(paddingValues),
            onProductClick = {
                // TODO: Navigasi ke halaman detail produk
            }
        )
    }
}

@Composable
fun DashboardContent(modifier: Modifier = Modifier, onProductClick: (Product) -> Unit) {

    // Data dummy untuk produk, kita gunakan URL gambar dari placeholder
    val newArrivals = listOf(
        Product("Succulent Plant", "PLANTS", "https://i.imgur.com/gX2L9aJ.png"),
        Product("Mobile Lens", "GEAR", "https://i.imgur.com/9vL2dF6.png"),
        Product("Yellow Letter", "DECOR", "https://i.imgur.com/uC5G1mN.png"),
        Product("Little Reader", "FIGURES", "https://i.imgur.com/V28p8aM.png"),
        Product("Yellow Headphones", "GEAR", "https://i.imgur.com/O6aJ3O7.png"),
        Product("Blue Speaker", "GEAR", "https://i.imgur.com/E8w9o4c.png"),
        Product("Colorful Pillows", "DECOR", "https://i.imgur.com/Yh7WfA9.png"),
        Product("Coffee Cup", "HOME", "https://i.imgur.com/5V3X4h9.png"),
        Product("Toy Car", "TOYS", "https://i.imgur.com/3f0i5vS.png")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Kita atur padding global di sini, bukan di dalam item
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // == Item 1: Banner Utama ==
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // --- PERBAIKAN: Ganti teks dengan gambar logo ---
                Image(
                    // Mengambil gambar dari folder res/drawable
                    painter = painterResource(id = R.drawable.logo), // Ganti 'hm_logo' dengan nama file Anda
                    contentDescription = "H&M Logo",
                    modifier = Modifier
                        .height(50.dp) // Atur tinggi logo sesuai keinginan Anda
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit // Gunakan ContentScale.Fit agar logo tidak terpotong
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Tombol "Read More"
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Text("READ MORE")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }

        // == Item 2: Judul "NEW ARRIVALS" ==
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEW ARRIVALS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // == Item 3: Grid Produk ==
        item {
            // Kita gunakan LazyVerticalGrid di dalam LazyColumn
            // Perhatikan penggunaan `userScrollEnabled = false` dan pengaturan tinggi
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 kolom seperti di desain
                modifier = Modifier
                    .fillMaxWidth()
                    // Tinggi grid dihitung berdasarkan jumlah baris
                    .height(((newArrivals.size + 1) / 2 * 280).dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false // Scrolling dikontrol oleh LazyColumn luar
            ) {
                items(newArrivals) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product) })
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }

        // == Item 4: Banner Diskon ==
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SAVE UP TO 30%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Text("READ MORE")
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar Produk
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Membuat gambar menjadi persegi
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Fit
            )
            // Detail Produk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.category,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}