package com.example.tugas1.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tugas1.ui.viewmodel.ChatMessage
import com.example.tugas1.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    var text by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { chatViewModel.sendImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("H&M", modifier = Modifier.padding(bottom = 6.dp)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Default.Image, null)
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tulis pesan...") }
                )

                IconButton(onClick = {
                    chatViewModel.sendText(text)
                    text = ""
                }) {
                    Icon(Icons.Default.Send, null)
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7)),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messages) { msg: ChatMessage ->

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        if (msg.sender == "user")
                            Alignment.End
                        else
                            Alignment.Start
                ) {

                    msg.imageUri?.let { image ->
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    msg.text?.let { messageText ->
                        Surface(
                            color = if (msg.sender == "user")
                                MaterialTheme.colorScheme.primary
                            else Color.White
                        ) {
                            Text(
                                messageText,
                                modifier = Modifier.padding(10.dp),
                                color = if (msg.sender == "user")
                                    Color.White
                                else Color.Black
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
