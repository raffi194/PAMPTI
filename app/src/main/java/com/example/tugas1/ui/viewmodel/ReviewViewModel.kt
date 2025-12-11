package com.example.tugas1.viewmodel

import android.content.Context // DITAMBAHKAN
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

// Data class ini tidak perlu diubah
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

    // DIUBAH: Tambahkan parameter 'context' untuk mengakses ContentResolver
    fun submitReview(
        context: Context, // DITAMBAHKAN
        orderId: String,
        rating: Int,
        comment: String,
        imageUris: List<Uri>,
        onComplete: (Boolean) -> Unit // DITAMBAHKAN: Callback untuk status
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    throw IllegalStateException("Pengguna tidak login.")
                }

                // --- BLOK YANG DIPERBAIKI ---
                val imageUrls = mutableListOf<String>()
                imageUris.forEach { uri ->
                    // 1. Dapatkan stream dari Uri menggunakan ContentResolver
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        // 2. Baca stream menjadi ByteArray
                        val byteArray = inputStream.readBytes()
                        val fileName = "${userId}/${UUID.randomUUID()}"

                        // 3. Upload ByteArray ke Supabase Storage
                        val uploadResult = SupabaseClient.client.storage["review_images"].upload(
                            path = fileName,
                            data = byteArray, // Sekarang kita menggunakan ByteArray
                            upsert = true
                        )
                        imageUrls.add(uploadResult)
                    }
                }
                // --- AKHIR BLOK YANG DIPERBAIKI ---

                val reviewPayload = ReviewPayload(
                    user_id = userId,
                    order_id = orderId,
                    product_id = "00000000-0000-0000-0000-000000000000", // TODO: Ganti dengan product_id asli nanti
                    rating = rating,
                    comment = comment.ifBlank { null },
                    image_urls = imageUrls.ifEmpty { null }
                )

                SupabaseClient.client.postgrest["reviews"].insert(reviewPayload)
                onComplete(true) // Panggil callback sukses

            } catch (e: Exception) {
                errorMessage.value = "Gagal mengirim ulasan: ${e.message}"
                e.printStackTrace() // Sangat membantu untuk debugging
                onComplete(false) // Panggil callback gagal
            } finally {
                isLoading.value = false
            }
        }
    }
}
