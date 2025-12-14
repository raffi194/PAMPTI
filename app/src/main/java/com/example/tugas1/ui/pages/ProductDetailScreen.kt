// app/src/main/java/com/example/tugas1/ui/pages/ProductDetailScreen.kt

package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    productId: String
) {
    val product by remember(productId) { mutableStateOf(productViewModel.getProductById(productId)) }
    val context = LocalContext.current

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Produk tidak ditemukan atau sedang dimuat...")
            LaunchedEffect(Unit) {
                productViewModel.fetchProducts()
            }
        }
        return
    }

    var productName by remember { mutableStateOf(product!!.name) }
    var productPrice by remember { mutableStateOf(product!!.price.toString()) }
    var productDesc by remember { mutableStateOf(product!!.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Detail Produk", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            val imageToShow = imageUri ?: product?.imageUrl
            if (imageToShow != null) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Gambar Produk",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Tidak Ada Gambar", color = Color.Gray)
            }
        }
        Spacer(Modifier.height(12.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Ganti Gambar")
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = productPrice,
            onValueChange = { productPrice = it },
            label = { Text("Harga") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = productDesc, onValueChange = { productDesc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(24.dp))

        // Tombol ini sekarang akan berfungsi karena ViewModel sudah benar
        Button(
            onClick = {
                productViewModel.updateProduct(
                    productId = productId,
                    name = productName,
                    price = productPrice.toDoubleOrNull() ?: 0.0,
                    description = productDesc,
                    newImageUri = imageUri,
                    context = context,
                    onComplete = {
                        navController.popBackStack()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Simpan Perubahan")
        }
    }
}
