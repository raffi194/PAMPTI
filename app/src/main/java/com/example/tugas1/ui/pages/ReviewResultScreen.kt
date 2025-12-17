package com.example.tugas1.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.model.Review
import com.example.tugas1.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewResultScreen(
    navController: NavController,
    orderId: String,
    productId: String,
    reviewViewModel: ReviewViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    var review by remember { mutableStateOf<Review?>(null) }
    val isLoading by reviewViewModel.isLoading
    val errorMessage by reviewViewModel.errorMessage

    // 🔥 Ambil review dari database
    LaunchedEffect(orderId, productId) {
        review = reviewViewModel.getReviewByOrderAndProduct(
            orderId = orderId,
            productId = productId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ulasan Anda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        when {
            isLoading -> {
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
                    Text(errorMessage ?: "Terjadi kesalahan")
                }
            }

            review == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ulasan tidak ditemukan")
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp)
                ) {

                    Text(
                        "Terima kasih! 🎉",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ⭐ RATING
                    Text("Rating", fontWeight = FontWeight.Bold)
                    Row {
                        repeat(review!!.rating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 💬 KOMENTAR
                    Text("Ulasan Anda", fontWeight = FontWeight.Bold)
                    Text(
                        review!!.comment ?: "-",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // 🖼 FOTO PRODUK
                    if (review?.image_urls?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Foto Produk", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow {
                            items(review!!.image_urls!!) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.navigate("order_history") {
                                popUpTo("order_history") { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali ke Riwayat Pesanan")
                    }
                }
            }
        }
    }
}