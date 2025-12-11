package com.example.tugas1.model

import android.net.Uri

data class Product(
    val id: String,
    var name: String,
    var price: Double,
    var description: String,
    var imageUrl: String? = null
)
