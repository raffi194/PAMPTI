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
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class ProductViewModel : ViewModel() {

    // ================= PRODUCT =================
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    // ================= UI STATE =================
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    // ================= CART =================
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    // ================= ORDER =================
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory = _orderHistory.asStateFlow()

    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess = _orderSuccess.asStateFlow()

    init {
        fetchProducts()
    }

    // ======================================================
    // 🔥 CREATE ORDER (STATE-BASED, AMAN NAVIGATION)
    // ======================================================
    fun createOrder() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Pengguna belum login")

                val currentCart = _cartItems.value
                if (currentCart.isEmpty()) {
                    throw IllegalStateException("Keranjang kosong")
                }

                // ---------- HITUNG TOTAL ----------
                val total = currentCart.sumOf {
                    it.product.price * it.quantity
                }

                // ---------- INSERT ORDER ----------
                val orderToInsert = OrderToInsert(
                    userId = currentUserId,
                    totalPrice = total
                )

                val newOrder = SupabaseClient.client.postgrest["orders"]
                    .insert(orderToInsert) { select() }
                    .decodeSingle<Order>()

                // ---------- INSERT ORDER ITEMS ----------
                val itemsToInsert = currentCart.map { cartItem ->
                    OrderItemToInsert(
                        orderId = newOrder.id,
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        pricePerItem = cartItem.product.price
                    )
                }

                SupabaseClient.client.postgrest["order_items"].insert(itemsToInsert)

                // ---------- SUCCESS ----------
                clearCart()
                _orderSuccess.value = true

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Gagal membuat pesanan: ${e.message}"
                _orderSuccess.value = false
            } finally {
                isLoading.value = false
            }
        }
    }

    // ======================================================
    // 🛒 CART FUNCTIONS
    // ======================================================
    fun addToCart(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null) {
                currentCart.map {
                    if (it.product.id == product.id)
                        it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                currentCart + CartItem(product = product, quantity = 1)
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { it.filterNot { cart -> cart.product.id == item.product.id } }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(item)
        } else {
            _cartItems.update { currentCart ->
                currentCart.map {
                    if (it.product.id == item.product.id)
                        it.copy(quantity = newQuantity)
                    else it
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun resetOrderSuccess() {
        _orderSuccess.value = false
    }

    // ======================================================
    // 📦 PRODUCT
    // ======================================================
    fun fetchProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                _products.value =
                    SupabaseClient.client.postgrest["products"].select().decodeList()
            } catch (e: Exception) {
                errorMessage.value = "Gagal memuat produk: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getProductById(productId: String): Product? =
        _products.value.find { it.id == productId }

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
                    context.contentResolver.openInputStream(newImageUri)?.use {
                        val bytes = it.readBytes()
                        val fileName = "product_images/${UUID.randomUUID()}"
                        val uploadPath = SupabaseClient.client.storage["products"]
                            .upload(fileName, bytes, upsert = true)
                        finalImageUrl = SupabaseClient.client.storage["products"]
                            .publicUrl(uploadPath)
                    }
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "price" to price,
                    "description" to description
                )
                finalImageUrl?.let { updates["image_url"] = it }

                SupabaseClient.client.postgrest["products"]
                    .update(updates) { filter { eq("id", productId) } }

                fetchProducts()
                onComplete()

            } catch (e: Exception) {
                errorMessage.value = "Gagal update produk: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // ======================================================
    // 📜 ORDER HISTORY
    // ======================================================
    fun fetchOrderHistory() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val orders = SupabaseClient.client.postgrest["orders"]
                    .select { order("created_at", SupabaseOrder.DESCENDING) }
                    .decodeList<Order>()

                val ordersWithItems = orders.map { order ->
                    val items = SupabaseClient.client.postgrest["order_items"]
                        .select { filter { eq("order_id", order.id) } }
                        .decodeList<OrderItem>()

                    order.copy(
                        items = items.map { item ->
                            item.copy(productDetails = getProductById(item.productId))
                        }
                    )
                }

                _orderHistory.value = ordersWithItems

            } catch (e: Exception) {
                errorMessage.value = "Gagal memuat riwayat pesanan: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    fun uploadPaymentProof(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val fileName = "payment_${System.currentTimeMillis()}.jpg"

                    // PAKAI API YANG SAMA SEPERTI updateProduct()
                    val bucket = SupabaseClient.client.storage["payment_proofs"]

                    val uploadPath = bucket.upload(
                        fileName,
                        bytes,
                        upsert = true
                    )

                    val publicUrl = bucket.publicUrl(uploadPath)

                    onSuccess(publicUrl)

                } ?: throw Exception("Tidak bisa membuka gambar")

            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}