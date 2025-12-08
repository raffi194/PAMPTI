package com.example.tugas1.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckoutViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow(
        listOf("Tas Wanita", "Sepatu Sport", "Jaket Hoodie")
    )
    val cartItems = _cartItems.asStateFlow()
}
