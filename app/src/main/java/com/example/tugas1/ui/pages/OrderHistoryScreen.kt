package com.example.tugas1.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.tugas1.util.toRupiahFormat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// --- Data Class untuk menampung data pesanan (sementara/dummy) ---
// Nantinya, ini akan menjadi model yang sesuai dengan tabel 'orders' di Supabase
data class Order(
    val id: String,
    val orderNumber: String,
    val date: Date,
    val totalPrice: Double,
    val status: String // "Diproses", "Dikirim", "Selesai", "Dibatalkan"
)

// --- Composable Utama untuk Layar Riwayat Pesanan ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(navController: NavController) {
    // Data dummy untuk ditampilkan di UI. Nanti kita akan mengambil ini dari ViewModel.
    val dummyOrders = listOf(
        Order("oid1", "INV/2025/12/01", Date(), 185000.0, "Selesai"),
        Order("oid2", "INV/2025/11/28", Date(), 95000.0, "Dikirim"),
        Order("oid3", "INV/2025/11/25", Date(), 320000.0, "Dibatalkan"),
        Order("oid4", "INV/2025/11/22", Date(), 55000.0, "Selesai"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan") },
                navigationIcon = {
                    // Tombol kembali, jika layar ini diakses dari halaman lain
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        // Cek jika daftar pesanan kosong
        if (dummyOrders.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            // Tampilkan daftar pesanan jika ada
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.05f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dummyOrders) { order ->
                    OrderHistoryCard(
                        order = order,
                        onReviewClick = {
                            // Navigasi ke halaman review, mengirimkan ID pesanan
                            navController.navigate("submit_review/${order.id}")
                        }
                    )
                }
            }
        }
    }
}

// --- Composable untuk setiap item pesanan di daftar ---
@Composable
fun OrderHistoryCard(order: Order, onReviewClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bagian Atas: Nomor Pesanan dan Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(order.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Bagian Tengah: Total Harga dan Status
            InfoRow(label = "Total Harga", value = order.totalPrice.toRupiahFormat())
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Status", value = order.status, valueColor = getStatusColor(order.status))

            // Bagian Bawah: Tombol Aksi (jika perlu)
            // Tombol "Beri Ulasan" hanya muncul jika status pesanan "Selesai"
            if (order.status == "Selesai") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onReviewClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Beri Ulasan")
                }
            }
        }
    }
}

// --- Composable pembantu untuk menampilkan baris info ---
@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

// --- Composable untuk state saat tidak ada pesanan ---
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Riwayat Kosong",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum Ada Riwayat Pesanan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Semua pesanan yang Anda buat akan muncul di sini.",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Selesai" -> Color(0xFF008000) // Hijau
        "Dikirim" -> Color(0xFF0000FF) // Biru
        "Diproses" -> Color(0xFFFFA500) // Oranye
        "Dibatalkan" -> Color.Red
        else -> Color.Black
    }
}
