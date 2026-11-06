package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
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
import com.example.models.CallRecord
import com.example.repository.VoiceGangRepository
import com.example.ui.components.VoiceGangHeader
import com.example.ui.theme.*

@Composable
fun CallsScreen(
    currentUserId: String,
    repository: VoiceGangRepository,
    onCallAgain: (targetUid: String, targetName: String, targetGangId: String, isVideo: Boolean) -> Unit
) {
    val callHistory by repository.observeCallHistory().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            VoiceGangHeader(
                title = "Call History",
                subtitle = "Recent WebRTC audio and video calls"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (callHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = VoiceGangPrimaryLight,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No calls yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Initiate an audio or video call by searching a user's 8-digit ID on Home!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(callHistory) { record ->
                    val isVideo = record.callType == "video"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("call_record_item_${record.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (record.status == "missed" || record.status == "rejected") VoiceGangAccentRed.copy(alpha = 0.2f)
                                        else VoiceGangAccentGreen.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (record.status == "missed") Icons.AutoMirrored.Filled.CallMissed
                                    else if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                    contentDescription = null,
                                    tint = if (record.status == "missed") VoiceGangAccentRed else VoiceGangAccentGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.receiverName.ifBlank { "Voice Gangster" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${record.callType.replaceFirstChar { it.uppercase() }} • ID: ${record.receiverVoiceGangId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VoiceGangPrimaryLight
                                    )
                                    if (record.durationSeconds > 0) {
                                        Text(
                                            text = " • ${record.durationSeconds}s",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    onCallAgain(record.receiverId, record.receiverName, record.receiverVoiceGangId, isVideo)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                    contentDescription = "Call Again",
                                    tint = if (isVideo) VoiceGangTertiary else VoiceGangSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
