package com.example.tugas1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tugas1.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Mengambil state dari ViewModel
    val authState by authViewModel.authState.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val loading by authViewModel.loading.collectAsState()

    // LaunchedEffect untuk navigasi HANYA saat authState berubah menjadi true
    // Ini lebih spesifik dan aman.
    LaunchedEffect(key1 = authState) {
        if (authState) {
            // Jika registrasi berhasil, authState menjadi true, lalu navigasi
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    // Menggunakan Box agar semua konten terpusat, mirip LoginScreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Padding di luar
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Jarak otomatis
        ) {
            Text(
                "Register",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null // Highlight field jika ada error
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null
            )

            Button(
                onClick = {
                    authViewModel.register(email, password)
                },
                enabled = !loading, // Tombol disable saat loading
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Register")
                }
            }

            // Tampilkan pesan error langsung di UI, bukan sebagai Toast
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            TextButton(onClick = { navController.navigate("login") }) {
                Text("Sudah punya akun? Login di sini")
            }
        }
    }
}
