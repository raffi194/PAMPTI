package com.example.tugas1.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tugas1.data.remote.SupabaseClient // <-- IMPORT SupabaseClient
import com.example.tugas1.viewmodel.AuthViewModel
import io.github.jan.supabase.gotrue.auth // <-- IMPORT auth extension

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    // --- PERBAIKAN 1: Pantau status authState untuk navigasi setelah logout ---
    val isAuthenticated by authViewModel.authState.collectAsState()

    // --- PERBAIKAN 2: Ambil email pengguna saat ini ---
    // Cara aman untuk mendapatkan user yang sedang login
    val currentUserEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: "Email tidak ditemukan"

    // LaunchedEffect untuk bereaksi ketika user sudah tidak terautentikasi (setelah logout)
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            // Jika user tidak lagi login, navigasi kembali ke halaman login.
            navController.navigate("auth_graph") {
                // Hapus semua halaman dari 'main_graph' agar tidak bisa kembali ke dashboard
                popUpTo("main_graph") {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nama: Nisa Aulia Harismadani", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // --- PERBAIKAN 3: Tampilkan email dinamis ---
            Text(text = "Email: $currentUserEmail", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // --- PERBAIKAN 4: Fungsikan tombol logout ---
            Button(
                onClick = {
                    authViewModel.logout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
