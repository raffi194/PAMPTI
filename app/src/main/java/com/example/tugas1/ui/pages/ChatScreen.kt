package com.example.tugas1.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tugas1.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chat") })
        }
    ) { padding ->

        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .clickable { navController.navigate("chat_detail") }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = rememberAsyncImagePainter(
                    "https://www.centralparkjakarta.com/upload/tenant/0h&m.jpg"
                ),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text("H&M", fontWeight = FontWeight.Bold)
                Text(
                    chatViewModel.lastMessage(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text("Hari ini", fontSize = 12.sp)
        }
    }
}
