package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
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
import com.example.models.UserProfile
import com.example.repository.VoiceGangRepository
import com.example.ui.components.VoiceGangHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    repository: VoiceGangRepository,
    isDarkTheme: Boolean = true,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profile?.displayName ?: "") }
    var editBio by remember { mutableStateOf(profile?.bio ?: "") }

    Scaffold(
        topBar = {
            VoiceGangHeader(
                title = "My Profile",
                subtitle = "Account settings & permanent VOICE GANG ID"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(listOf(VoiceGangPrimary, VoiceGangSecondary, VoiceGangTertiary, VoiceGangPrimary))
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(VoiceGangSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile?.displayName?.take(1)?.uppercase() ?: "V",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoiceGangSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profile?.displayName ?: "Voice Gangster",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "@${profile?.username ?: "voicegang"}",
                style = MaterialTheme.typography.bodyMedium,
                color = VoiceGangPrimaryLight
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permanent 8-digit VOICE GANG ID banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("VOICE GANG ID", profile?.voiceGangId ?: "")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Voice Gang ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("profile_voice_gang_id_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UNIQUE VOICE GANG ID",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = VoiceGangPrimaryLight
                        )
                        Text(
                            text = profile?.voiceGangId ?: "83749261",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = VoiceGangSecondary
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("VOICE GANG ID", profile?.voiceGangId ?: "")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Voice Gang ID copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = VoiceGangSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio / About Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ABOUT / BIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = {
                            editName = profile?.displayName ?: ""
                            editBio = profile?.bio ?: ""
                            showEditDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = VoiceGangPrimary)
                        }
                    }
                    Text(
                        text = profile?.bio?.ifBlank { "Hey! I'm on VOICE GANG. Connect with my 8-digit ID!" } ?: "Ready to connect!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme & Color Customizer Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = VoiceGangPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "APP THEME & COLORS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Customize the look from dark cyber to light/clean or modern neon accents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onToggleDarkTheme(true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) VoiceGangPrimary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                "Dark Cyber",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { onToggleDarkTheme(false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDarkTheme) VoiceGangSecondary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                "Light Clean",
                                fontSize = 12.sp,
                                color = if (!isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Privacy Info
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = VoiceGangAccentGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Direct Discovery Architecture",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No friend requests required. Anyone with your 8-digit ID can message or call you securely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VoiceGangAccentRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Voice Gang", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditDialog = false
                        coroutineScope.launch {
                            repository.updateProfile(editName.trim(), editBio.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoiceGangPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
