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
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 1. Blok init dihapus untuk mencegah pemanggilan otomatis
    // init {
    //     getProfile()
    // }

    // 2. Nama fungsi diubah dari getProfile menjadi loadProfile
    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Pengguna tidak login")

                val profileData: Profile? = SupabaseClient.client.postgrest
                    .from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull()

                if (profileData == null) {
                    // Pastikan model Profile Anda memiliki parameter avatar_url
                    val newProfile = Profile(id = userId, fullName = "Nama Belum Diatur", username = "username", avatar_url = null)
                    SupabaseClient.client.postgrest.from("profiles").insert(newProfile)
                    _profile.value = newProfile
                } else {
                    _profile.value = profileData
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil profil: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // 3. Fungsi clearProfile ditambahkan
    fun clearProfile() {
        _profile.value = null
        _errorMessage.value = null
    }

    fun updateProfile(fullName: String, username: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val updates = mapOf(
                    "full_name" to fullName,
                    "username" to username
                )
                SupabaseClient.client.postgrest.from("profiles").update(updates) {
                    filter {
                        eq("id", userId)
                    }
                }
                // Panggil loadProfile untuk sinkronisasi setelah update
                loadProfile()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui profil: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadAvatar(imageBytes: ByteArray) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val filePath = "$userId/avatar.png"

                val publicUrl = SupabaseClient.client.storage.from("avatars").publicUrl(filePath)

                SupabaseClient.client.storage.from("avatars").upload(
                    path = filePath,
                    data = imageBytes,
                    upsert = true
                )

                val updates = mapOf("avatar_url" to publicUrl) // Simpan URL lengkap
                SupabaseClient.client.postgrest.from("profiles").update(updates) {
                    filter { eq("id", userId) }
                }

                // Panggil loadProfile untuk sinkronisasi setelah update
                loadProfile()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengunggah avatar: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Fungsi ini tidak lagi seefisien menyimpan URL publik langsung, tapi bisa dipertahankan jika perlu
    fun getAvatarPublicUrl(path: String): StateFlow<String?> {
        val urlState = MutableStateFlow<String?>(null)
        viewModelScope.launch {
            try {
                val url = SupabaseClient.client.storage.from("avatars").createSignedUrl(path, 300.seconds)
                urlState.value = url
            } catch (e: Exception) {
                urlState.value = null
            }
        }
        return urlState
    }
}
