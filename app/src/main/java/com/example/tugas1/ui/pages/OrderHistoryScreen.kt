// app/src/main/java/com/example/tugas1/ui/pages/OrderHistoryScreen.kt

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
import com.example.tugas1.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    val reviewViewModel: ReviewViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        productViewModel.fetchOrderHistory()
    }

    val orderHistory by productViewModel.orderHistory.collectAsState()
    val isLoading by productViewModel.isLoading
    val errorMessage by productViewModel.errorMessage

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
    ) { padding ->
        when {
            isLoading && orderHistory.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $errorMessage")
                }
            }

            orderHistory.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Anda belum memiliki riwayat pesanan.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orderHistory, key = { it.id }) { order ->

                        val productId = order.items.firstOrNull()?.productId
                        var isReviewed by remember { mutableStateOf(false) }

                        LaunchedEffect(order.id) {
                            if (productId != null) {
                                isReviewed = reviewViewModel.hasReviewed(
                                    orderId = order.id,
                                    productId = productId
                                )
                            }
                        }

                        OrderHistoryCard(
                            order = order,
                            isLoading = isLoading,
                            isReviewed = isReviewed,
                            onUpdateStatus = { newStatus ->
                                productViewModel.updateOrderStatus(order.id, newStatus) { success ->
                                    Toast.makeText(
                                        context,
                                        if (success) "Status diubah"
                                        else "Gagal mengubah status",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onReviewClick = {
                                if (productId != null) {
                                    navController.navigate(
                                        "submit_review/${order.id}/$productId"
                                    )
                                }
                            },
                            onViewReviewClick = {
                                if (productId != null) {
                                    navController.navigate(
                                        "review_result/${order.id}/$productId"
                                    )
                                }
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
    isReviewed: Boolean,
    onUpdateStatus: (String) -> Unit,
    onReviewClick: () -> Unit,
    onViewReviewClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Order #${order.id.take(8)}",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = order.status,
                color = when (order.status) {
                    "Selesai" -> Color(0xFF2E7D32)
                    "Dibatalkan" -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity}x ${item.productDetails?.name ?: "Produk"}")
                    Text((item.pricePerItem * item.quantity).toRupiahFormat())
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Total: ${order.totalPrice.toRupiahFormat()}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (order.status == "Diproses") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onUpdateStatus("Dibatalkan") },
                        enabled = !isLoading
                    ) {
                        Text("Batalkan")
                    }
                    Button(
                        onClick = { onUpdateStatus("Selesai") },
                        enabled = !isLoading
                    ) {
                        Text("Pesanan Diterima")
                    }
                }
            }

            if (order.status == "Selesai") {
                Spacer(modifier = Modifier.height(8.dp))
                if (isReviewed) {
                    OutlinedButton(onClick = onViewReviewClick) {
                        Text("Lihat Ulasan")
                    }
                } else {
                    Button(onClick = onReviewClick) {
                        Text("Beri Ulasan")
                    }
                }
            }
        }
    }
}

fun formatSupabaseDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            Locale.getDefault()
        )
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        formatter.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}
