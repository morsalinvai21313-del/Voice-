package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import com.example.models.Group
import com.example.repository.VoiceGangRepository
import com.example.ui.components.VoiceGangHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    currentUserId: String,
    repository: VoiceGangRepository,
    onOpenGroupRoom: (Group) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val groups by repository.observeGroups().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            VoiceGangHeader(
                title = "Groups & Voice Lounges",
                subtitle = "Scalable multi-user audio/video rooms",
                onAddClick = { showCreateDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = VoiceGangPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_create_group")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
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
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = VoiceGangPrimaryLight,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No groups yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Create the first Voice Gang community and launch a shared live audio room!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoiceGangPrimary)
                    ) {
                        Text("Create Group")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onOpenGroupRoom(group) }
                            .testTag("group_card_${group.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
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
                                                text = "VOICE ACTIVE",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
                                onClick = { onOpenGroupRoom(group) },
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

    // Create Group Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("Create Voice Gang Group", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_group_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newGroupDesc,
                        onValueChange = { newGroupDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_group_desc"),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            val name = newGroupName.trim()
                            val desc = newGroupDesc.trim()
                            showCreateDialog = false
                            newGroupName = ""
                            newGroupDesc = ""
                            coroutineScope.launch {
                                repository.createGroup(name, desc)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoiceGangPrimary),
                    enabled = newGroupName.isNotBlank(),
                    modifier = Modifier.testTag("confirm_create_group_btn")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
