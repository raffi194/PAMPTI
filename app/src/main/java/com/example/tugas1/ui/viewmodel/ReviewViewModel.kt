// PASTI BENAR: app/src/main/java/com/example/tugas1/viewmodel/ReviewViewModel.kt

package com.example.tugas1.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.text.input.insert
// --- KUNCI PERBAIKAN: HAPUS BARIS IMPORT DI BAWAH INI ---
// import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import com.example.tugas1.model.Review // Pastikan import ini ada
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning // Pastikan import ini ada
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@kotlinx.serialization.Serializable
data class ReviewPayload(
    val user_id: String,
    val product_id: String,
    val order_id: String,
    val rating: Int,
    val comment: String?,
    val image_urls: List<String>?
)

class ReviewViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews = _userReviews.asStateFlow()

    init {
        fetchUserReviews()
    }

    fun fetchUserReviews() {
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val reviews: List<Review> = SupabaseClient.client.postgrest["reviews"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList()
                _userReviews.value = reviews
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitReview(
        context: Context,
        orderId: String,
        productId: String,
        rating: Int,
        comment: String,
        imageUris: List<Uri>,
        onComplete: (Boolean) -> Unit
    ) {
        if (productId.isBlank() || productId == "00000000-0000-0000-0000-000000000000") {
            errorMessage.value = "ID Produk tidak valid."
            onComplete(false)
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Pengguna tidak login.")

                val imageUrls = mutableListOf<String>()
                imageUris.forEach { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val byteArray = inputStream.readBytes()
                        val fileExtension = getFileExtension(context, uri) ?: "jpg"
                        val fileName = "${userId}/${UUID.randomUUID()}.$fileExtension"

                        val uploadPath = SupabaseClient.client.storage["review_image"].upload(
                            path = fileName,
                            data = byteArray,
                            upsert = true
                        )

                        val publicUrl = SupabaseClient.client.storage["review_image"].publicUrl(uploadPath)
                        imageUrls.add(publicUrl)
                    }
                }

                val payload = ReviewPayload(
                    user_id = userId,
                    order_id = orderId,
                    product_id = productId,
                    rating = rating,
                    comment = comment.ifBlank { null },
                    image_urls = imageUrls.ifEmpty { null }
                )

                // Dengan import yang salah dihapus, baris di bawah ini sekarang akan berfungsi dengan benar
                SupabaseClient.client.postgrest["reviews"].insert(
                    payload
                )

                fetchUserReviews()
                onComplete(true)

            } catch (e: Exception) {
                errorMessage.value = "Gagal mengirim ulasan: ${e.message}"
                e.printStackTrace()
                onComplete(false)
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }
    }
    suspend fun getReviewByOrderAndProduct(
        orderId: String,
        productId: String
    ): Review? {
        return try {
            SupabaseClient.client
                .postgrest["reviews"]
                .select {
                    filter {
                        eq("order_id", orderId)
                        eq("product_id", productId)
                    }
                }
                .decodeSingleOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun hasReviewed(orderId: String, productId: String): Boolean {
        return try {
            val result = SupabaseClient.client
                .postgrest["reviews"]
                .select {
                    filter {
                        eq("order_id", orderId)
                        eq("product_id", productId)
                    }
                }
                .decodeList<Review>()

            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}