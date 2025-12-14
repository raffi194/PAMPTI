package com.example.tugas1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant // Gunakan Instant untuk tipe data timestamp

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,

    // 1. Buat non-nullable dan beri nilai default jika Anda ingin selalu ada teks
    val description: String = "Tidak ada deskripsi.",

    // 2. Gunakan @SerialName untuk konsistensi
    @SerialName("image_url")
    val imageUrl: String?, // Tetap nullable karena gambar bisa saja tidak ada

    val category: String?,

    // 3. Gunakan tipe data yang benar untuk timestamp
    @SerialName("created_at")
    val createdAt: Instant
)