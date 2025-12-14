package com.example.tugas1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id")
    val orderId: String,
    @SerialName("product_id")
    val productId: String,
    val quantity: Int,
    @SerialName("price_per_item")
    val pricePerItem: Double,
    // Kita akan isi ini secara manual
    var productDetails: Product? = null
)

@Serializable
data class OrderItemToInsert(
    @SerialName("order_id")
    val orderId: String,
    @SerialName("product_id")
    val productId: String,
    val quantity: Int,
    @SerialName("price_per_item")
    val pricePerItem: Double
)
