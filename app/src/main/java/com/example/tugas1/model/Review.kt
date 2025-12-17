// Lokasi file: app/src/main/java/com/example/tugas1/model/Review.kt

package com.example.tugas1.model //<- Perubahan: Sesuaikan package ke 'model'

import kotlinx.serialization.Serializable

/**
 * Mewakili struktur data ulasan yang diterima dari database Supabase.
 * Properti di sini harus cocok dengan kolom di tabel 'reviews' Anda.
 * @Serializable memungkinkan Supabase untuk mengubah JSON menjadi objek Kotlin ini.
 */
@kotlinx.serialization.Serializable
data class Review(
    val id: String,
    val user_id: String,
    val order_id: String,
    val product_id: String,
    val rating: Int,
    val comment: String?,
    val image_urls: List<String>? = emptyList()
)

/**
 * Mewakili data yang akan DIKIRIM ke Supabase saat membuat ulasan baru.
 * Memisahkan Payload dari Model utama adalah praktik yang baik.
 */
@Serializable
data class ReviewPayload(
    val user_id: String,
    val product_id: String,
    val order_id: String,
    val rating: Int,
    val comment: String?,
    val image_urls: List<String>?
)
