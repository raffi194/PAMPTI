// PASTI BENAR: app/src/main/java/com/example/tugas1/ui/pages/OrderHistoryScreen.kt

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tugas1.model.Order
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    // Panggil fungsi untuk mengambil data saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        productViewModel.fetchOrderHistory()
    }

    val orderHistory by productViewModel.orderHistory.collectAsState()
    val isLoading by productViewModel.isLoading
    val errorMessage by productViewModel.errorMessage
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center
        ) {
            // Tampilkan loading indicator hanya jika data sedang dimuat dan list masih kosong
            if (isLoading && orderHistory.isEmpty()) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text("Error: $errorMessage")
            } else if (orderHistory.isEmpty()) {
                Text("Anda belum memiliki riwayat pesanan.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orderHistory, key = { it.id }) { order ->
                        // --- KUNCI PERUBAHAN: Mengirimkan fungsi ke Card ---
                        OrderHistoryCard(
                            order = order,
                            isLoading = isLoading,
                            onUpdateStatus = { newStatus ->
                                productViewModel.updateOrderStatus(order.id, newStatus) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Status pesanan diubah!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal mengubah status.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onReviewClick = {
                                // Navigasi ke halaman review dengan membawa ID pesanan
                                navController.navigate("submit_review/${order.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: Order,
    isLoading: Boolean,
    onUpdateStatus: (newStatus: String) -> Unit,
    onReviewClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // --- KUNCI PERUBAHAN: Warna status dinamis ---
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (order.status) {
                        "Selesai" -> Color(0xFF2E7D32) // Hijau Tua
                        "Dibatalkan" -> Color(0xFFC62828) // Merah Tua
                        "Diproses" -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray
                    }
                )
            }
            Text(
                text = formatSupabaseDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Tampilkan item-item di dalam order
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("${item.quantity}x ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        item.productDetails?.name ?: "Produk tidak ditemukan",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        (item.pricePerItem * item.quantity).toRupiahFormat(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    order.totalPrice.toRupiahFormat(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // --- KUNCI PERUBAHAN: Tombol Aksi Dinamis ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tampilkan tombol Batal & Pesanan Diterima HANYA jika status masih "Diproses"
                if (order.status == "Diproses") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("Dibatalkan") },
                        enabled = !isLoading,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Batalkan")
                    }
                    Button(
                        onClick = { onUpdateStatus("Selesai") },
                        enabled = !isLoading,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Pesanan Diterima")
                    }
                }

                // Tampilkan tombol Beri Ulasan HANYA jika status "Selesai"
                if (order.status == "Selesai") {
                    Button(
                        onClick = onReviewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Warna hijau
                    ) {
                        Text("Beri Ulasan")
                    }
                }
            }
        }
    }
}

// Fungsi helper untuk format tanggal (tidak berubah)
fun formatSupabaseDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        formatter.format(date!!)
    } catch (e: Exception) {
        dateString // Return original string if parsing fails
    }
}