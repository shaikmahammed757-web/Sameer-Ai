package com.example.aisameer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AISameerTheme {
                AISameerHome()
            }
        }
    }
}

val BlueAI = Color(0xFF19C8FF)
val PurpleAI = Color(0xFF7B35FF)
val DarkBG = Color(0xFF050817)
val CardBG = Color(0xFF10172C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISameerHome() {
    var message by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBG,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBG
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Brush.linearGradient(listOf(BlueAI, PurpleAI))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "AI Sameer",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your Personal AI Assistant",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF090D1D)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Tools") },
                    label = { Text("Tools") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Hello 👋",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "How can I help you today?",
                color = Color.LightGray,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            AIBanner()

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "What can I do for you?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(aiFeatures) { feature ->
                    FeatureCard(feature = feature)
                }
            }

            ChatInput(
                value = message,
                onValueChange = { message = it },
                onSend = {
                    if (message.isNotBlank()) {
                        message = ""
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun AIBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF123D8A), Color(0xFF30116B), Color(0xFF111A45))
                )
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "✨ AI Sameer",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Think • Create • Achieve",
                color = BlueAI,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your smart companion for ideas,\ncreativity and productivity.",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 25.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF3DDAFF), Color.Transparent)))
        )
    }
}

data class AIFeature(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val aiFeatures = listOf(
    AIFeature("AI Chat", "Ask anything", Icons.Default.Chat),
    AIFeature("Voice Assistant", "Talk naturally", Icons.Default.Mic),
    AIFeature("Image", "Understand photos", Icons.Default.Image),
    AIFeature("Documents", "PDF & files", Icons.Default.Description),
    AIFeature("Writing", "Create & edit", Icons.Default.Edit),
    AIFeature("AI Images", "Create visuals", Icons.Default.Palette),
    AIFeature("YouTube", "Titles & scripts", Icons.Default.PlayArrow),
    AIFeature("Web Search", "Find information", Icons.Default.Search)
)

@Composable
fun FeatureCard(feature: AIFeature) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBG)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(BlueAI, PurpleAI))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = feature.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = feature.subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF151D35))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Default.Add, contentDescription = "Attach", tint = Color.White)
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask AI Sameer...", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        IconButton(onClick = {}) {
            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = BlueAI)
        }

        IconButton(onClick = onSend) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BlueAI, PurpleAI))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun AISameerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = BlueAI,
            secondary = PurpleAI,
            background = DarkBG,
            surface = CardBG
        ),
        content = content
    )
}
