package com.example.tugas1.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tugas1.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    // Terima ViewModel yang sama dari MainActivity
    profileViewModel: ProfileViewModel
) {
    // Ambil data profil dari ViewModel
    val profile by profileViewModel.profile.collectAsState()
    val loading by profileViewModel.loading.collectAsState()

    // State untuk menampung input dari TextField
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    // PERBAIKAN PENTING: Gunakan LaunchedEffect untuk mengisi TextField
    // dengan data dari ViewModel saat layar pertama kali dibuka atau saat data profil berubah.
    LaunchedEffect(profile) {
        profile?.let {
            fullName = it.fullName ?: ""
            username = it.username ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Panggil fungsi update di ViewModel dengan data dari TextField
                    profileViewModel.updateProfile(fullName, username)
                    // Setelah selesai, kembali ke halaman sebelumnya (ProfileScreen)
                    navController.navigateUp()
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save")
                }
            }
        }
    }
}