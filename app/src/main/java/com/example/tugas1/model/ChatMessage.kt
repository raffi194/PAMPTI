package com.example.tugas1.ui.viewmodel

data class ChatMessage(
    val sender: String,
    val text: String? = null,
    val imageUri: String? = null
)
