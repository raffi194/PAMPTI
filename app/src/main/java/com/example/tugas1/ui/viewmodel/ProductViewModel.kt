// app/src/main/java/com/example/tugas1/viewmodel/ProductViewModel.kt

package com.example.tugas1.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import com.example.tugas1.model.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder // Alias untuk menghindari konflik
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class ProductViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory = _orderHistory.asStateFlow()

    init {
        fetchProducts()
    }
    // Tambahkan fungsi ini di dalam kelas ProductViewModel
    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Menggunakan .update() untuk mengubah kolom 'status' pada baris yang cocok
                SupabaseClient.client.postgrest["orders"]
                    .update(mapOf("status" to newStatus)) {
                        filter {
                            eq("id", orderId)
                        }
                    }

                // Setelah berhasil update, panggil fetchOrderHistory() untuk
                // merefresh data di UI secara otomatis
                fetchOrderHistory()
                onComplete(true)

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Gagal mengubah status pesanan: ${e.message}"
                onComplete(false)
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- KUNCI PENYELESAIAN ADA DI FUNGSI INI ---
    fun createOrder(onComplete: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Pengguna tidak login. Tidak bisa membuat pesanan.")

                val currentCart = _cartItems.value
                if (currentCart.isEmpty()) {
                    throw IllegalStateException("Keranjang kosong. Tidak ada yang di-checkout.")
                }

                // 1. Masukkan data ke tabel 'orders'
                val total = currentCart.sumOf { it.product.price * it.quantity }

                // Menggunakan struktur data yang sesuai dengan tabel 'orders' Anda
                val orderToInsert = OrderToInsert(
                    userId = currentUserId,
                    totalPrice = total
                    // 'order_number' dan 'status' akan diisi oleh nilai default di database
                )

                val newOrder = SupabaseClient.client.postgrest["orders"]
                    .insert(orderToInsert) { select() } // Ambil data yang baru saja di-insert
                    .decodeSingle<Order>() // Decode menjadi objek Order

                // 2. Siapkan dan masukkan data ke 'order_items'
                val itemsToInsert = currentCart.map { cartItem ->
                    OrderItemToInsert(
                        orderId = newOrder.id,
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        pricePerItem = cartItem.product.price
                    )
                }

                SupabaseClient.client.postgrest["order_items"].insert(itemsToInsert)

                // 3. Jika berhasil, bersihkan keranjang dan panggil callback sukses
                clearCart()
                onComplete(true, null)

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = "Gagal membuat pesanan: ${e.message}"
                errorMessage.value = errorMsg
                onComplete(false, errorMsg)
            } finally {
                isLoading.value = false
            }
        }
    }

    // ... (Sisa kode ProductViewModel lainnya tidak berubah) ...
    // Pastikan fungsi-fungsi ini ada
    fun fetchProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                _products.value = SupabaseClient.client.postgrest["products"].select().decodeList()
            } catch (e: Exception) {
                errorMessage.value = "Gagal memuat produk: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateProduct(
        productId: String,
        name: String,
        price: Double,
        description: String,
        newImageUri: Uri?,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                var finalImageUrl: String? = getProductById(productId)?.imageUrl

                if (newImageUri != null) {
                    context.contentResolver.openInputStream(newImageUri)?.use { inputStream ->
                        val byteArray = inputStream.readBytes()
                        val fileName = "product_images/${UUID.randomUUID()}"
                        val uploadPath = SupabaseClient.client.storage["products"].upload(
                            path = fileName,
                            data = byteArray,
                            upsert = true
                        )
                        finalImageUrl = SupabaseClient.client.storage["products"].publicUrl(uploadPath)
                    }
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "price" to price,
                    "description" to description
                )
                finalImageUrl?.let { updates["image_url"] = it }

                SupabaseClient.client.postgrest["products"]
                    .update(updates) {
                        filter { eq("id", productId) }
                    }

                fetchProducts()
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Gagal mengupdate produk: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addToCart(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null) {
                currentCart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
            } else {
                currentCart + CartItem(product = product, quantity = 1)
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { currentCart -> currentCart.filterNot { it.product.id == item.product.id } }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(item)
        } else {
            _cartItems.update { currentCart ->
                currentCart.map { if (it.product.id == item.product.id) it.copy(quantity = newQuantity) else it }
            }
        }
    }

    fun clearCart() { _cartItems.value = emptyList() }

    fun getProductById(productId: String): Product? = _products.value.find { it.id == productId }

    fun fetchOrderHistory() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val orders = SupabaseClient.client.postgrest["orders"]
                    .select { order("created_at", SupabaseOrder.DESCENDING) }
                    .decodeList<Order>()

                val ordersWithDetails = orders.map { order ->
                    val items = SupabaseClient.client.postgrest["order_items"]
                        .select { filter { eq("order_id", order.id) } }
                        .decodeList<OrderItem>()

                    val itemsWithProductDetails = items.map { item ->
                        val product = getProductById(item.productId)
                        item.copy(productDetails = product)
                    }
                    order.copy(items = itemsWithProductDetails)
                }

                _orderHistory.value = ordersWithDetails

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Gagal memuat riwayat pesanan: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
