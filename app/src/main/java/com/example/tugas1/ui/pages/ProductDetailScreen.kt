package com.example.tugas1.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.R
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String?, // ID bisa null jika terjadi error navigasi
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current

    // Cek apakah productId valid
    if (productId == null) {
        // Tampilkan pesan error dan tombol kembali jika ID tidak ada
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ID Produk tidak ditemukan.", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Kembali ke Dashboard")
                    }
                }
            }
        }
        return // Hentikan eksekusi Composable
    }

    // Ambil detail produk dari ViewModel menggunakan ID
    // getProductById adalah fungsi yang efisien karena tidak perlu call network lagi
    val product = productViewModel.getProductById(productId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Jika produk tidak ditemukan, tampilkan loading atau pesan error
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                // Mungkin produk sedang dimuat atau ID-nya salah
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Produk tidak ditemukan atau sedang dimuat...")
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        } else {
            // Jika produk ditemukan, tampilkan detailnya
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()) // Agar bisa di-scroll jika konten panjang
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_background)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.price.toRupiahFormat(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Deskripsi Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp // Agar lebih mudah dibaca
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            productViewModel.addToCart(product)
                            Toast.makeText(context, "${product.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Tambahkan ke Keranjang", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}