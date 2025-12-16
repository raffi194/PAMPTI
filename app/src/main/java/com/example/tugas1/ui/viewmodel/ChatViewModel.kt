package com.example.tugas1.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                sender = "shop",
                text = "Halo Kak 👋 Ada yang bisa kami bantu?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendText(text: String) {
        if (text.isBlank()) return

        _messages.value = _messages.value +
                ChatMessage(sender = "user", text = text)

        viewModelScope.launch {
            repository.sendTextMessage(text)
        }
    }

    fun sendImage(uri: Uri) {
        _messages.value = _messages.value +
                ChatMessage(sender = "user", imageUri = uri.toString())

        viewModelScope.launch {
            repository.sendImageMessage(uri)
        }
    }

    fun lastMessage(): String {
        val last = _messages.value.lastOrNull()
        return last?.text ?: if (last?.imageUri != null) "📷 Gambar" else ""
    }
}
