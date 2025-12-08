package com.example.tugas1.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.tugas1.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductViewModel : ViewModel() {

    private val _productList = MutableStateFlow(
        listOf(
            Product("1", "Laptop Pro", 12000000, "Laptop kencang untuk kerja", ""),
            Product("2", "Keyboard Gaming", 350000, "RGB full warna", ""),
            Product("3", "Mouse Wireless", 150000, "Nyaman dan ringan", "")
        )
    )
    val productList: StateFlow<List<Product>> = _productList

    // Ambil product berdasarkan ID
    fun getProductById(id: String): Product? {
        return _productList.value.firstOrNull { it.id == id }
    }

    // Update product
    fun updateProduct(
        id: String,
        name: String,
        price: Int,
        description: String,
        imageUri: Uri?
    ) {
        val current = _productList.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }

        if (index != -1) {
            val old = current[index]

            current[index] = old.copy(
                name = name,
                price = price,
                description = description,
                imageUrl = imageUri?.toString() ?: old.imageUrl
            )

            _productList.value = current
        }
    }

    private val _cartItems = MutableStateFlow<List<Product>>(emptyList())
    val cartItems: StateFlow<List<Product>> = _cartItems

    fun addToCart(product: Product) {
        _cartItems.value = _cartItems.value + product
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}
