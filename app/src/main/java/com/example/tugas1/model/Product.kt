package com.example.tugas1.model


import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Mewakili struktur data produk yang dibaca dari database.
 * Digunakan untuk menampilkan produk di aplikasi.
 */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,


    // Dibuat non-nullable dengan nilai default agar aman saat ditampilkan.
    val description: String = "Tidak ada deskripsi.",


    // Gunakan @SerialName jika nama kolom di database berbeda (snake_case vs camelCase).
    @SerialName("image_url")
    val imageUrl: String?, // Dibuat nullable karena gambar bisa saja tidak ada.


    val category: String?,


    // Gunakan tipe data Instant untuk kolom timestamp/timestamptz.
    @SerialName("created_at")
    val createdAt: Instant
)


/**
 * Mewakili struktur data yang akan dikirim ke database saat membuat produk baru.
 * Hanya berisi kolom yang perlu diisi secara manual.
 */
@Serializable
data class ProductToInsert(
    val name: String,
    val price: Double,
    val description: String,
    @SerialName("image_url")
    val imageUrl: String
)