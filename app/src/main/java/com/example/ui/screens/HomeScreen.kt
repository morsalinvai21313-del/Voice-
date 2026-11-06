package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Group
import com.example.models.UserProfile
import com.example.models.VoiceGangIdDirectory
import com.example.repository.VoiceGangRepository
import com.example.ui.components.UserDiscoveryCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    myProfile: UserProfile?,
    repository: VoiceGangRepository,
    onOpenChat: (targetUid: String, targetName: String, targetGangId: String) -> Unit,
    onStartCall: (targetUid: String, targetName: String, targetGangId: String, isVideo: Boolean) -> Unit,
    onJoinGroupRoom: (Group) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchIdInput by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<VoiceGangIdDirectory?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }

    val activeGroups by repository.observeGroups().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Welcome & Profile ID Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_profile_badge"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(VoiceGangPrimary.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(VoiceGangPrimary, VoiceGangSecondary))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = myProfile?.displayName?.take(1)?.uppercase() ?: "V",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = myProfile?.displayName ?: "Voice Gangster",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ready to connect & talk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoiceGangAccentGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Permanent 8-digit unique ID banner with one-tap copy
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = VoiceGangDarkBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("VOICE GANG ID", myProfile?.voiceGangId ?: "")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Voice Gang ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("my_voice_gang_id_badge")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "YOUR PERMANENT VOICE GANG ID",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = VoiceGangPrimaryLight
                                    )
                                    Text(
                                        text = myProfile?.voiceGangId ?: "Generating...",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        ),
                                        color = VoiceGangSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy ID",
                                    tint = VoiceGangPrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FIND PEOPLE / SEARCH BY 8-DIGIT UNIQUE ID
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "FIND PEOPLE / SEARCH USER ID",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = VoiceGangSecondary
            )
            Text(
                text = "Connect instantly with any user using their 8-digit ID without friend requests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchIdInput,
                    onValueChange = {
                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                            searchIdInput = it
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_user_id_input"),
                    placeholder = { Text("Enter 8-digit ID (e.g. 83749261)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoiceGangSecondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        if (searchIdInput.length == 8) {
                            isSearching = true
                            searchError = null
                            searchResult = null
                            coroutineScope.launch {
                                val result = repository.findUserByVoiceGangId(searchIdInput)
                                isSearching = false
                                if (result != null) {
                                    searchResult = result
                                } else {
                                    searchError = "No user found with ID $searchIdInput"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("search_user_id_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VoiceGangSecondary),
                    enabled = searchIdInput.length == 8 && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }

            if (searchError != null) {
                Text(
                    text = searchError ?: "",
                    color = VoiceGangAccentRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Search Result Card
        if (searchResult != null) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                val foundUser = searchResult!!
                UserDiscoveryCard(
                    user = foundUser,
                    onMessageClick = {
                        onOpenChat(foundUser.userId, foundUser.displayName, foundUser.voiceGangId)
                    },
                    onAudioCallClick = {
                        onStartCall(foundUser.userId, foundUser.displayName, foundUser.voiceGangId, false)
                    },
                    onVideoCallClick = {
                        onStartCall(foundUser.userId, foundUser.displayName, foundUser.voiceGangId, true)
                    }
                )
            }
        }

        // Active Group Audio Rooms & Communities
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE VOICE ROOMS & GROUPS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = VoiceGangPrimaryLight
                )
            }
        }

        if (activeGroups.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = VoiceGangPrimaryLight,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active voice rooms right now",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create a group in the Groups tab to start talking!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeGroups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onJoinGroupRoom(group) }
                        .testTag("group_room_card_${group.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (group.isVoiceActive) VoiceGangAccentGreen else VoiceGangPrimary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (group.isVoiceActive) Icons.Default.RecordVoiceOver else Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (group.isVoiceActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = VoiceGangAccentGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "LIVE VOICE",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = VoiceGangAccentGreen
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${group.memberUids.size} members • ${group.description.ifBlank { "Voice Gang lounge" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onJoinGroupRoom(group) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (group.isVoiceActive) VoiceGangAccentGreen else VoiceGangPrimary
                            )
                        ) {
                            Text(if (group.isVoiceActive) "Join Voice" else "Open")
                        }
                    }
                }
            }
        }
    }
}
