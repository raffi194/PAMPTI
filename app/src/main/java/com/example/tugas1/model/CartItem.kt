package com.example.tugas1.model

// Data class ini membungkus Product dan menambahkan quantity
data class CartItem(
    val product: Product,
    var quantity: Int = 1
)
