// Pastikan path package benar
package com.example.tugas1.model

// DITAMBAHKAN: Impor anotasi yang diperlukan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DITAMBAHKAN: Anotasi ini adalah kunci perbaikannya
@Serializable
data class Product(
    // Properti 'id' seringkali dibuat otomatis oleh Supabase, jadi pastikan ada di select Anda
    val id: String,

    val name: String,
    val price: Double,
    val description: String?, // Dibuat nullable (?) jika bisa kosong di database

    // DIWAJIBKAN JIKA NAMA BEDA:
    // Gunakan @SerialName jika nama variabel di Kotlin (imageUrl)
    // berbeda dari nama kolom di Supabase (image_url). Ini praktik terbaik.
    @SerialName("image_url")
    val imageUrl: String?,

    val category: String?,

    // Lakukan hal yang sama untuk created_at
    @SerialName("created_at")
    val createdAt: String
)
