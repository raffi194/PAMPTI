package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.model.CartItem
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


data class PaymentMethod(
    val id: String,
    val name: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {

    // ===== CART SNAPSHOT =====
    val cartItems by productViewModel.cartItems.collectAsState()
    val cartSnapshot: List<CartItem> = remember { cartItems }

    val totalPrice = cartSnapshot.sumOf {
        it.product.price * it.quantity
    }

    // ===== ORDER SUCCESS OBSERVER =====
    val orderSuccess by productViewModel.orderSuccess.collectAsState()

    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            navController.navigate("order_success") {
                popUpTo("cart") { inclusive = true }
            }
            productViewModel.resetOrderSuccess()
        }
    }

    // ===== UI STATE =====
    var selectedPaymentId by remember { mutableStateOf<String?>(null) }
    var paymentProofUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            paymentProofUri = it
        }

    val paymentMethods = listOf(
        PaymentMethod("bank", "Transfer Bank", "BCA • BRI • Mandiri • BNI"),
        PaymentMethod("ewallet", "E-Wallet", "OVO • DANA • GoPay • ShopeePay"),
        PaymentMethod("cod", "COD", "Bayar di tempat")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ===== ALAMAT =====
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Alamat Pengiriman", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Nama Penerima: Naylah Yasmin\nAlamat: Jl. Veteran No.1\nKota Malang, Jawa Timur")
                    }
                }

                // ===== PAYMENT =====
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Metode Pembayaran", fontWeight = FontWeight.Bold)
                        paymentMethods.forEach {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentId = it.id }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedPaymentId == it.id)
                                                MaterialTheme.colorScheme.primary
                                            else Color.LightGray
                                        )
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(it.name)
                                    Text(it.description, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // ===== UPLOAD =====
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Bukti Pembayaran", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { pickImageLauncher.launch("image/*") }
                            ) {
                                Icon(Icons.Default.Image, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pilih Gambar")
                            }
                            Spacer(Modifier.width(12.dp))
                            paymentProofUri?.let {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // ===== RINGKASAN =====
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ringkasan Belanja", fontWeight = FontWeight.Bold)
                        cartSnapshot.forEach {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${it.product.name} x${it.quantity}")
                                Text((it.product.price * it.quantity).toRupiahFormat())
                            }
                        }
                        Divider(Modifier.padding(vertical = 8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Text(totalPrice.toRupiahFormat(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ===== BUTTON =====
            val context = LocalContext.current

            Button(
                enabled = selectedPaymentId != null && paymentProofUri != null,
                onClick = {
                    val uri = paymentProofUri ?: return@Button

                    productViewModel.uploadPaymentProof(
                        context = context,
                        imageUri = uri,
                        onSuccess = {
                            productViewModel.createOrder()
                            navController.navigate("order_success")
                        },
                        onError = { error ->
                            Toast.makeText(
                                context,
                                "Upload gagal: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
            ) {
                Text("Buat Pesanan")
            }
        }
    }
}