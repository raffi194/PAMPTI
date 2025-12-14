package com.example.tugas1.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.R
import com.example.tugas1.model.Product
import com.example.tugas1.ui.nav.AppBottomNavigation
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo Toko",
                            modifier = Modifier
                                .height(32.dp), // Beri tinggi eksplisit
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                actions = {
                    // Ikon keranjang di pojok kanan atas
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang Belanja"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { innerPadding ->
        // Bungkus konten dengan Box yang diberi padding agar tidak tumpang tindih
        Box(modifier = Modifier.padding(innerPadding)) {
            DashboardContent(
                productViewModel = productViewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun DashboardContent(
    productViewModel: ProductViewModel,
    navController: NavController
) {
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading
    val errorMessage by productViewModel.errorMessage

    // KUNCI PERBAIKAN: Mengganti LazyColumn dengan LazyVerticalGrid sebagai root layout.
    // Ini adalah cara yang benar untuk memiliki item yang span penuh dan item grid.
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Tetap 2 kolom untuk produk
        modifier = Modifier.fillMaxSize(),
        // Beri padding untuk konten di dalamnya agar tidak menempel ke tepi layar
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // == Item 1: Banner Utama ==
        // Menggunakan span agar item ini memakan lebar 2 kolom.
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), // Disesuaikan sedikit
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Text("READ MORE")
                }
                Spacer(modifier = Modifier.height(32.dp)) // Jarak antar seksi
            }
        }

        // == Item 2: Judul "NEW ARRIVALS" ==
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("NEW ARRIVALS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Produk terbaru dan terbaik hanya untuk Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // == Item 3: Grid Produk ==
        if (isLoading) {
            item(span = { GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (errorMessage != null) {
            item(span = { GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
        } else if (products.isEmpty() && !isLoading) {
            item(span = { GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada produk yang tersedia.")
                }
            }
        } else {
            // Ini akan merender setiap produk dalam satu sel grid.
            // Tidak perlu menghitung tinggi manual, LazyVerticalGrid akan mengaturnya.
            items(products) { product ->
                ProductCardLive(
                    product = product,
                    onCardClick = {
                        navController.navigate("product_detail/${product.id}")
                    },
                    onAddToCart = {
                        productViewModel.addToCart(product)
                    }
                )
            }
        }

        // == Item 4: Banner Diskon ==
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), // Memberi jarak dari grid produk
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SAVE UP TO 30%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Penawaran terbatas untuk produk-produk pilihan.",
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
}


// ProductCardLive tidak perlu diubah, sudah benar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCardLive(
    product: Product,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onCardClick
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.price.toRupiahFormat(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
                    Text("Add to Cart")
                }
            }
        }
    }
}
