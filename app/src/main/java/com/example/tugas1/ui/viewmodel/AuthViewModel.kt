package com.example.tugas1.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugas1.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {


    private val _authState = MutableStateFlow(false)
    val authState: StateFlow<Boolean> = _authState


    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    init {
        // Cek session secara realtime (Supabase auto-refresh session)
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collectLatest { status ->
                _authState.value = status is SessionStatus.Authenticated
            }
        }
    }


    fun register(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Jangan set authState = true, karena user BELUM login di Supabase
                _errorMessage.value = "Register berhasil! Cek email untuk verifikasi."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Register gagal"
            } finally {
                _loading.value = false
            }
        }
    }


    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }


                if (_authState.value) {
                    onSuccess()
                } else {
                    onError("Login gagal: email belum terverifikasi?")
                }


            } catch (e: Exception) {
                val errorMsg = e.message ?: "Login gagal"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _loading.value = false
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Logout gagal"
            } finally {
                _loading.value = false
            }
        }
    }
}
