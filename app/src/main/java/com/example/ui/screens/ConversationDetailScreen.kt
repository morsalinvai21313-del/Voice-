package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Message
import com.example.repository.VoiceGangRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ConversationDetailScreen(
    conversationId: String,
    peerName: String,
    peerGangId: String,
    peerUid: String,
    currentUserId: String,
    myName: String,
    repository: VoiceGangRepository,
    onBack: () -> Unit,
    onStartCall: (targetUid: String, targetName: String, targetGangId: String, isVideo: Boolean) -> Unit
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    val messages by repository.observeMessages(conversationId).collectAsState(initial = emptyList())
    var messageInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VoiceGangPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = peerName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = peerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "VOICE GANG ID: $peerGangId",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VoiceGangSecondary
                        )
                    }

                    IconButton(
                        onClick = { onStartCall(peerUid, peerName, peerGangId, false) },
                        modifier = Modifier.testTag("chat_audio_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = VoiceGangSecondary)
                    }

                    IconButton(
                        onClick = { onStartCall(peerUid, peerName, peerGangId, true) },
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = VoiceGangTertiary)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) VoiceGangPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                if (msg.type == "voice") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (isMe) Color.White else VoiceGangSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Voice message",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.sendMessage(conversationId, "🎤 [Voice message 0:15]", myName, "voice")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Send Voice Message", tint = VoiceGangPrimary)
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        placeholder = { Text("Type message...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VoiceGangPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                val text = messageInput.trim()
                                messageInput = ""
                                coroutineScope.launch {
                                    repository.sendMessage(conversationId, text, myName, "text")
                                }
                            }
                        },
                        enabled = messageInput.isNotBlank(),
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageInput.isNotBlank()) VoiceGangPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
