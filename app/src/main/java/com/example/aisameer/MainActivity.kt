package com.example.aisameer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

val BlueAI = Color(0xFF1976D2)
val PurpleAI = Color(0xFF7B1FA2)
val DarkBG = Color(0xFF0F172A)
val CardBG = Color(0xFF1E293B)
val TextGray = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSameerScreen() {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // మీ AIzaSy... API Key ని ఇక్కడ ఉంచండి
    val apiKey = "YOUR_AIZASY_API_KEY_HERE"

    Scaffold(
        containerColor = DarkBG,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBG),
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Text("☰", color = Color.White, fontSize = 22.sp)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(BlueAI, PurpleAI))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Ai Sameer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Online • Ready to help", color = Color(0xFF4ADE80), fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("హలో! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("నేను Ai Sameer ని. నన్ను ఏదైనా అడగండి!", fontSize = 16.sp, color = TextGray)
                    }
                }
            } else {
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
                                shape = RoundedCornerShape(16.dp)
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
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = PurpleAI
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Ai Sameer...", color = TextGray) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBG,
                        unfocusedContainerColor = CardBG,
                        disabledContainerColor = CardBG,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
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
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(BlueAI, PurpleAI)))
                ) {
                    Text("➔", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
