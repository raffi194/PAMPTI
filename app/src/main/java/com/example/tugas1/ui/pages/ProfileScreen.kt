package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.data.remote.SupabaseClient
import com.example.tugas1.viewmodel.AuthViewModel
import com.example.tugas1.viewmodel.ProfileViewModel
import io.github.jan.supabase.storage.storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    val profile by profileViewModel.profile.collectAsState()
    val loading by profileViewModel.loading.collectAsState()
    val isAuthenticated by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val imageBytes = inputStream.readBytes()
                    profileViewModel.uploadAvatar(imageBytes)
                }
            }
        }
    )

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate("auth_graph") {
                popUpTo("main_graph") { inclusive = true }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading && profile == null) {
                CircularProgressIndicator()
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.BottomEnd) {

                    // --- PERBAIKAN DI SINI ---
                    // State untuk menampung URL gambar yang akan ditampilkan
                    var avatarDisplayUrl by remember { mutableStateOf<String?>(null) }

                    // LaunchedEffect untuk mendapatkan URL publik saat path avatar berubah
                    LaunchedEffect(profile?.avatarUrl) {
                        profile?.avatarUrl?.let { path ->
                            // Panggil nama fungsi yang benar: getAvatarPublicUrl
                            profileViewModel.getAvatarPublicUrl(path).collect { url ->
                                avatarDisplayUrl = url
                            }
                        }
                    }
                    // ------------------------

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = avatarDisplayUrl ?: "https://i.pravatar.cc/150" // URL default
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Picture",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(6.dp)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.fullName ?: "Nama Belum Diatur",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${profile?.username ?: "username"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    onClick = { navController.navigate("editProfile") }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification",
                    onClick = { navController.navigate("notification") }
                )
                ProfileMenuItem(
                    icon = Icons.Default.PinDrop,
                    title = "Shipping Address",
                    onClick = { /* TODO */ }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Key,
                    title = "Change Password",
                    onClick = { /* TODO */ }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontSize = 16.sp, color = Color.Red)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Composable ProfileMenuItem tidak perlu diubah
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF4A55A2))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray)
    }
}
