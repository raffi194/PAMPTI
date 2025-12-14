// app/src/main/java/com/example/tugas1/ui/pages/CheckoutScreen.kt

package com.example.tugas1.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.R
import com.example.tugas1.model.CartItem
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    val cartItems by productViewModel.cartItems.collectAsState()
    val finalTotalPrice = cartItems.sumOf { it.product.price * it.quantity }
    val isLoading by productViewModel.isLoading
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        if (cartItems.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada item untuk di-checkout.")
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    items(cartItems, key = { it.product.id }) { cartItem ->
                        CheckoutItemCard(cartItem = cartItem)
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Pembayaran:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = finalTotalPrice.toRupiahFormat(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        // --- INI TOMBOL YANG DITANYAKAN ---
                        Button(
                            // Tombol akan non-aktif saat proses loading berlangsung
                            enabled = !isLoading,
                            onClick = {
                                // Panggil fungsi createOrder dari ViewModel
                                productViewModel.createOrder { success, error ->
                                    if (success) {
                                        Toast.makeText(context, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                        // Arahkan ke halaman utama/dashboard dan hapus semua backstack
                                        navController.navigate("dashboard") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // Tampilkan pesan error jika gagal
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            // Tampilkan loading indicator di dalam tombol jika sedang loading
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("Konfirmasi dan Bayar")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable untuk menampilkan setiap item di halaman checkout
@Composable
fun CheckoutItemCard(cartItem: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cartItem.product.imageUrl,
            contentDescription = cartItem.product.name,
            modifier = Modifier.size(64.dp).padding(end = 16.dp),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cartItem.product.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Jumlah: ${cartItem.quantity}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = (cartItem.product.price * cartItem.quantity).toRupiahFormat(),
            fontWeight = FontWeight.SemiBold
        )
    }
}
