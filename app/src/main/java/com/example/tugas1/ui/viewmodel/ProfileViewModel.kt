package com.example.tugas1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import com.example.tugas1.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    /* -------------------- STATE -------------------- */

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    /* -------------------- LOAD / READ -------------------- */

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User belum login")

                val profileData = SupabaseClient.client.postgrest
                    .from("profiles")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<Profile>()

                if (profileData == null) {
                    // INSERT jika profile belum ada
                    val newProfile = Profile(
                        id = userId,
                        fullName = "Nama Belum Diatur",
                        username = "username",
                        avatar_url = null
                    )

                    SupabaseClient.client.postgrest
                        .from("profiles")
                        .insert(newProfile)

                    _profile.value = newProfile
                } else {
                    _profile.value = profileData
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    /* -------------------- UPDATE PROFILE -------------------- */

    fun updateProfile(fullName: String, username: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: return@launch

                SupabaseClient.client.postgrest
                    .from("profiles")
                    .update(
                        mapOf(
                            "full_name" to fullName,
                            "username" to username
                        )
                    ) {
                        filter { eq("id", userId) }
                    }

                // refresh data
                loadProfile()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    /* -------------------- UPLOAD AVATAR (STORAGE) -------------------- */

    fun uploadAvatar(imageBytes: ByteArray) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: return@launch

                // 🔹 gunakan file unik supaya cache tidak mengganggu
                val filePath = "$userId/avatar_${System.currentTimeMillis()}.png"

                // Upload ke Supabase Storage
                SupabaseClient.client.storage
                    .from("avatars")
                    .upload(
                        path = filePath,
                        data = imageBytes,
                        upsert = true
                    )

                // Ambil public URL
                val publicUrl = SupabaseClient.client.storage
                    .from("avatars")
                    .publicUrl(filePath)

                // Simpan URL ke database
                SupabaseClient.client.postgrest
                    .from("profiles")
                    .update(mapOf("avatar_url" to publicUrl)) {
                        filter { eq("id", userId) }
                    }

                // Refresh profile agar UI otomatis update
                loadProfile()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    /* -------------------- CLEAR STATE (LOGOUT) -------------------- */

    fun clearProfile() {
        _profile.value = null
        _loading.value = false
        _errorMessage.value = null
    }
}
