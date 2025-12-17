// /app/src/main/java/com/example/tugas1/ui/pages/ProductDetailScreen.kt

package com.example.tugas1.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.R
import com.example.tugas1.model.Product // Pastikan import ini ada
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String?, // Terima ID produk dari navigasi
    productViewModel: ProductViewModel = viewModel()
) {
    // --- KUNCI PERBAIKAN ---
    // 1. Ambil seluruh daftar produk sebagai state.
    val products by productViewModel.products.collectAsState()

    // 2. Cari produk yang sesuai dari daftar tersebut.
    // Ini akan otomatis dievaluasi ulang jika 'products' atau 'productId' berubah.
    val product: Product? = products.find { it.id == productId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Tombol kembali
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Jika ID produknya null atau produknya tidak ditemukan, tampilkan pesan.
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                // Tampilkan loading jika daftar produk masih kosong (sedang dimuat)
                if (products.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    Text("Produk tidak ditemukan atau ID tidak valid.")
                }
            }
            return@Scaffold
        }

        // --- Mulai dari sini, kita bisa yakin 'product' tidak null ---
        ProductDetailContent(
            modifier = Modifier.padding(innerPadding),
            product = product,
            onAddToCart = {
                productViewModel.addToCart(product)
                // Memberi notifikasi bahwa produk berhasil ditambahkan
                Toast.makeText(navController.context, "${product.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Kembali ke halaman sebelumnya
            }
        )
    }
}

@Composable
private fun ProductDetailContent(
    modifier: Modifier = Modifier,
    product: Product,
    onAddToCart: () -> Unit
) {
    // Gunakan Column dengan verticalScroll agar konten bisa di-scroll jika tidak muat.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Gambar Produk
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background) // Gambar fallback
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nama Produk
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Harga Produk
        Text(
            text = product.price.toRupiahFormat(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Deskripsi Produk
        Text(
            text = "Deskripsi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tombol "Add to Cart"
        Button(
            onClick = onAddToCart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tambah ke Keranjang")
        }
    }
}