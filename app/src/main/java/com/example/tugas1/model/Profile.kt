package com.example.tugas1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,

    @SerialName("full_name")
    val fullName: String? = null,

    val username: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null
)
