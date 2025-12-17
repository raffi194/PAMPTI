package com.example.tugas1.data.remote

import android.net.Uri
import com.example.tugas1.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class ChatRepository {

    suspend fun sendTextMessage(text: String) {
        SupabaseClient.client.postgrest
            .from("chat_messages")
            .insert(
                mapOf(
                    "chat_id" to "H&M_CHAT",
                    "sender" to "user",
                    "message" to text,
                    "image_url" to null
                )
            )
    }

    suspend fun sendImageMessage(uri: Uri) {
        SupabaseClient.client.postgrest
            .from("chat_messages")
            .insert(
                mapOf(
                    "chat_id" to "H&M_CHAT",
                    "sender" to "user",
                    "message" to null,
                    "image_url" to uri.toString()
                )
            )
    }
}
