package com.example.tugas1.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.R
import com.example.tugas1.model.CartItem // <-- Gunakan CartItem
import com.example.tugas1.util.toRupiahFormat
import com.example.tugas1.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, productViewModel: ProductViewModel) {

    val cartItems by productViewModel.cartItems.collectAsState()
    var selectedItemIds by remember(cartItems) { mutableStateOf(cartItems.map { it.product.id }.toSet()) }

    val selectedItems = cartItems.filter { it.product.id in selectedItemIds }
    // Total harga sekarang menghitung kuantitas
    val totalPrice = selectedItems.sumOf { it.product.price * it.quantity }
    val selectedItemsCount = selectedItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart (${cartItems.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Keranjang Anda masih kosong.")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        val areAllSelected = selectedItemIds.size == cartItems.size
                        selectedItemIds = if (areAllSelected) emptySet() else cartItems.map { it.product.id }.toSet()
                    }) {
                        Text(if (selectedItemIds.size == cartItems.size) "Deselect All" else "Select All")
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems, key = { it.product.id }) { item ->
                        RealCartItemCard(
                            item = item,
                            isSelected = item.product.id in selectedItemIds,
                            // MENGHUBUNGKAN FUNGSI REMOVE & QUANTITY
                            onRemoveClick = { productViewModel.removeFromCart(item) },
                            onQuantityChange = { newQuantity -> productViewModel.updateQuantity(item, newQuantity) },
                            onSelectionChange = { isSelected ->
                                val mutableSet = selectedItemIds.toMutableSet()
                                if (isSelected) mutableSet.add(item.product.id) else mutableSet.remove(item.product.id)
                                selectedItemIds = mutableSet
                            }
                        )
                    }
                }

                Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total ($selectedItemsCount items):", style = MaterialTheme.typography.titleLarge)
                        Text(totalPrice.toRupiahFormat(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        enabled = selectedItemsCount > 0,
                        onClick = { navController.navigate("checkout") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed to Checkout", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RealCartItemCard(
    item: CartItem, // <-- Terima CartItem
    isSelected: Boolean,
    onRemoveClick: () -> Unit,
    onQuantityChange: (Int) -> Unit, // <-- Callback baru untuk kuantitas
    onSelectionChange: (Boolean) -> Unit
) {
    val product = item.product // Ekstrak produk dari CartItem

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(checked = isSelected, onCheckedChange = onSelectionChange, modifier = Modifier.align(Alignment.CenterVertically))
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                product.category?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                Spacer(modifier = Modifier.height(8.dp))
                // Menampilkan harga TOTAL (harga satuan * kuantitas)
                Text(
                    text = (product.price * item.quantity).toRupiahFormat(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Menampilkan harga satuan jika kuantitas > 1
                if (item.quantity > 1) {
                    Text(
                        text = "(${product.price.toRupiahFormat()} / item)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // MENAMBAHKAN KEMBALI QUANTITY SELECTOR
                    QuantitySelector(quantity = item.quantity, onQuantityChange = onQuantityChange)
                    TextButton(onClick = onRemoveClick) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp).background(Color(0xFFE0E0E0), CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
        }
        Text(text = "$quantity", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp).background(Color(0xFFE0E0E0), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
        }
    }
}
