package com.example.tugas1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,

    @SerialName("full_name")
    val fullName: String? = null,

    val username: String? = null,

    @SerialName("avatar_url") // Anotasi ini memetakan ke kolom 'avatar_url' di Supabase
    val avatar_url: String? = null // Propert
)
