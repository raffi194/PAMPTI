package com.example.tugas1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_price")
    val totalPrice: Double,
    val status: String,
    // Kita akan mengisi list ini secara manual setelah mengambil data
    var items: List<OrderItem> = emptyList()
)

@Serializable
data class OrderToInsert(
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_price")
    val totalPrice: Double,
    val status: String = "Pesanan Diterima"
)
