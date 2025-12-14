package com.example.tugas1.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap // DITAMBAHKAN: Untuk mendapatkan ekstensi file
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

    fun submitReview(
        context: Context,
        orderId: String,
        rating: Int,
        comment: String,
        imageUris: List<Uri>,
        onComplete: (Boolean) -> Unit
    ) {
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

                        // --- BLOK YANG DIPERBAIKI ---
                        // 1. Dapatkan ekstensi file dari Uri (contoh: "jpg", "png")
                        val fileExtension = getFileExtension(context, uri)

                        // 2. Buat nama file yang unik DAN menyertakan ekstensi file
                        val fileName = "${userId}/${UUID.randomUUID()}.$fileExtension"
                        // --- AKHIR BLOK YANG DIPERBAIKI ---

                        // Upload ByteArray ke Supabase Storage
                        val uploadResult = SupabaseClient.client.storage["review_images"].upload(
                            path = fileName,
                            data = byteArray,
                            upsert = true
                        )
                        imageUrls.add(uploadResult)
                    }
                }

                val reviewPayload = ReviewPayload(
                    user_id = userId,
                    order_id = orderId,
                    product_id = "00000000-0000-0000-0000-000000000000", // TODO: Ganti dengan product_id asli nanti
                    rating = rating,
                    comment = comment.ifBlank { null },
                    image_urls = imageUrls.ifEmpty { null }
                )

                SupabaseClient.client.postgrest["reviews"].insert(reviewPayload)
                onComplete(true) // Sukses

            } catch (e: Exception) {
                // Pesan error yang lebih ramah pengguna
                errorMessage.value = "Gagal mengirim ulasan. Silakan coba lagi."
                e.printStackTrace()
                onComplete(false) // Gagal
            } finally {
                isLoading.value = false
            }
        }
    }
}

// DITAMBAHKAN: Fungsi helper untuk mendapatkan ekstensi file dari URI
private fun getFileExtension(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)?.let { mimeType ->
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}
