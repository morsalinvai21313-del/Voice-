package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Group
import com.example.repository.VoiceGangRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GroupVoiceRoomScreen(
    group: Group,
    currentUserId: String,
    repository: VoiceGangRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(false) }
    var isVoiceRoomActive by remember { mutableStateOf(group.isVoiceActive) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isVoiceRoomActive) VoiceGangAccentGreen else VoiceGangPrimaryLight)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVoiceRoomActive) "Voice Room Active • SFU Media Router" else "Voice Room Inactive",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isVoiceRoomActive) VoiceGangAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val nextState = !isVoiceRoomActive
                            isVoiceRoomActive = nextState
                            coroutineScope.launch {
                                repository.toggleGroupVoiceRoom(group.id, nextState)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVoiceRoomActive) VoiceGangAccentRed else VoiceGangAccentGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isVoiceRoomActive) "Leave Room" else "Start Voice")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isVoiceRoomActive) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) VoiceGangAccentRed else VoiceGangSurfaceVariant)
                                .testTag("group_voice_mute_btn")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                isVoiceRoomActive = false
                                coroutineScope.launch {
                                    repository.toggleGroupVoiceRoom(group.id, false)
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(VoiceGangAccentRed)
                                .testTag("group_voice_end_btn")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Disconnect", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPEAKERS & PARTICIPANTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = VoiceGangPrimaryLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Participants Grid
            val mockSpeakers = if (isVoiceRoomActive) {
                listOf(
                    Pair("You (Speaking)", true),
                    Pair("Alex ID:59281743", false),
                    Pair("Jordan ID:71638492", true),
                    Pair("Sam ID:83749261", false)
                )
            } else {
                listOf(Pair("You (Offline)", false))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mockSpeakers) { (name, isSpeaking) ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSpeaking) VoiceGangAccentGreen else VoiceGangPrimary
                                    )
                                    .padding(if (isSpeaking) 3.dp else 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
