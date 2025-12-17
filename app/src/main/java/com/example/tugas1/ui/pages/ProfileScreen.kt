package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val profile by profileViewModel.profile.collectAsState()
    val loading by profileViewModel.loading.collectAsState()
    val context = LocalContext.current

    // 🔴 STATE UNTUK AVATAR LOKAL (INI YANG PENTING)
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // tampilkan langsung di UI
            localAvatarUri = it

            // upload ke server
            context.contentResolver.openInputStream(it)?.use { input ->
                profileViewModel.uploadAvatar(input.readBytes())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("edit_profile") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F5FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ================= HEADER PROFILE ================= */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {

                        Image(
                            painter = rememberAsyncImagePainter(
                                model = localAvatarUri
                                    ?: profile?.avatar_url
                                    ?: "https://i.pravatar.cc/150"
                            ),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1C4E9)),
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7E57C2))
                                .padding(6.dp)
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile?.fullName ?: "-",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "@${profile?.username ?: "-"}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            /* ================= ACCOUNT SECTION ================= */
            item {
                ProfileMenuSection(title = "Account")
            }

            item {
                ProfileMenuItem(
                    title = "Home",
                    onClick = { navController.navigate("dashboard") }
                )
            }

            item {
                ProfileMenuItem(
                    title = "Order History",
                    onClick = { navController.navigate("order_history") }
                )
            }

            item {
                ProfileMenuItem(
                    title = "Chat Support",
                    onClick = { navController.navigate("chat") }
                )
            }

            /* ================= ACTION ================= */
            item {
                Spacer(modifier = Modifier.height(172.dp))
            }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEEEEE)
                    )
                ) {
                    Text("Logout", color = Color.Red)
                }
            }
        }
    }
}

/* ================= REUSABLE COMPONENT ================= */

@Composable
fun ProfileMenuSection(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF6A1B9A)
    )
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}