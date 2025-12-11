package com.example.tugas1.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import com.example.tugas1.model.Product // Pastikan path ke model Product Anda benar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    // State untuk menampung daftar produk
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    // State untuk loading dan error
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        // Langsung panggil fungsi fetchProducts saat ViewModel pertama kali dibuat
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Mengambil semua data dari tabel 'products' dan mengubahnya menjadi List<Product>
                val productList = SupabaseClient.client.postgrest["products"].select().decodeList<Product>()
                _products.value = productList
            } catch (e: Exception) {
                errorMessage.value = "Gagal memuat produk: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}
