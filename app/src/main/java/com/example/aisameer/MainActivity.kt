package com.example.aisameer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AiSameerScreen()
            }
        }
    }
}

val DarkBG = Color(0xFF0F172A)
val CardBG = Color(0xFF1E293B)
val PurpleAI = Color(0xFF7B1FA2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSameerScreen() {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val apiKey = "YOUR_AIZASY_API_KEY_HERE"

    Scaffold(
        containerColor = DarkBG,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBG),
                title = { Text("Ai Sameer", color = Color.White, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages) { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (msg.isUser) PurpleAI else CardBG,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = PurpleAI
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Ai Sameer...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val userMsg = inputText
                            messages.add(ChatMessage(userMsg, true))
                            inputText = ""
                            isLoading = true
                            scope.launch {
                                try {
                                    val model = GenerativeModel(
                                        modelName = "gemini-1.5-flash",
                                        apiKey = apiKey
                                    )
                                    val response = model.generateContent(userMsg)
                                    val responseText = response.text ?: "సమాధానం లభించలేదు."
                                    messages.add(ChatMessage(responseText, false))
                                } catch (e: Exception) {
                                    messages.add(ChatMessage("Error: ${e.localizedMessage}", false))
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    }
}

