package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch

// --- Composable Utama untuk Layar Tulis Ulasan ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReviewScreen(navController: NavController, orderId: String) {
    // DITAMBAHKAN: Inisialisasi ViewModel, states, dan context
    val reviewViewModel: ReviewViewModel = viewModel()
    val isLoading by reviewViewModel.isLoading
    val errorMessage by reviewViewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // States untuk menampung input dari pengguna
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Launcher untuk memilih gambar dari galeri (tidak berubah)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            selectedImages = uris
        }
    )

    // DITAMBAHKAN: Menampilkan pesan error jika ada menggunakan Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tulis Ulasan") },
                navigationIcon = {
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
        },
        // Tombol Kirim di bagian bawah
        bottomBar = {
            Button(
                // DIUBAH: onClick sekarang memanggil ViewModel dengan semua parameter yang benar
                onClick = {
                    reviewViewModel.submitReview(
                        context = context,
                        orderId = orderId,
                        rating = rating,
                        comment = comment,
                        imageUris = selectedImages,
                        onComplete = { success ->
                            // Navigasi kembali HANYA jika submit berhasil
                            if (success) {
                                navController.popBackStack()
                            }
                            // Jika gagal, Snackbar akan menampilkan pesan error.
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                // DIUBAH: Tombol dinonaktifkan saat loading atau rating belum diisi
                enabled = rating > 0 && !isLoading
            ) {
                // DIUBAH: Menampilkan CircularProgressIndicator saat loading
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Kirim Ulasan", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Apa pendapatmu tentang pesanan ini?", style = MaterialTheme.typography.titleLarge)
            Text("Pesanan ID: $orderId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Komponen Rating Bintang
            StarRatingSelector(
                rating = rating,
                onRatingChange = { newRating -> rating = newRating }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 2. Komponen Text Field untuk Komentar
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Tulis ulasanmu di sini...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Komponen Upload Foto
            Text(
                "Tambahkan Foto Produk",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ImageUploader(
                selectedImages = selectedImages,
                onAddImageClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }
    }
}


// --- Composable untuk memilih rating bintang --- (Tidak Berubah)
@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Rating $i",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChange(i) },
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray
            )
        }
    }
}

// --- Composable untuk mengunggah dan menampilkan gambar --- (Tidak Berubah)
@Composable
fun ImageUploader(
    selectedImages: List<Uri>,
    onAddImageClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol untuk menambah gambar
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onAddImageClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Tambah Foto",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Daftar gambar yang sudah dipilih
        items(selectedImages) { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
