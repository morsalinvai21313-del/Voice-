package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.webrtc.CallState
import com.example.webrtc.VoiceGangCallManager
import com.example.webrtc.WebRtcCallSession
import kotlinx.coroutines.delay

@Composable
fun WebRtcCallOverlayScreen(
    session: WebRtcCallSession,
    callManager: VoiceGangCallManager,
    onCallTerminated: () -> Unit
) {
    BackHandler {
        callManager.endCall()
        onCallTerminated()
    }

    // Call duration timer
    LaunchedEffect(session.callState) {
        if (session.callState == CallState.CONNECTED) {
            var seconds = 0L
            while (true) {
                delay(1000)
                seconds++
                callManager.updateDuration(seconds)
            }
        }
    }

    // Calling pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VoiceGangDarkBg, Color(0xFF0F172A), VoiceGangDarkBg)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VoiceGangPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = if (session.isVideo) "WEBRTC VIDEO CALL" else "WEBRTC AUDIO CALL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = if (session.isVideo) VoiceGangTertiary else VoiceGangSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = when (session.callState) {
                        CallState.OUTGOING_CALL -> "Calling..."
                        CallState.INCOMING_CALL -> "Incoming Call..."
                        CallState.CONNECTED -> {
                            val mins = session.durationSeconds / 60
                            val secs = session.durationSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        else -> "Connecting..."
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (session.callState == CallState.CONNECTED) VoiceGangAccentGreen else VoiceGangPrimaryLight
                )

                Text(
                    text = "STUN/TURN Signaling Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Center: Video feed if video call, else Avatar and Name
            if (session.isVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.CameraPreviewView(
                        isFrontCamera = session.isFrontCamera,
                        isCameraEnabled = session.isCameraEnabled,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Peer info chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = session.peerName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${session.peerVoiceGangId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VoiceGangSecondary
                            )
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(if (session.callState != CallState.CONNECTED) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(VoiceGangPrimary, VoiceGangSecondary, VoiceGangTertiary, VoiceGangPrimary)
                                )
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(VoiceGangSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = session.peerName.take(1).uppercase(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = session.peerName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VoiceGangSecondary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "VOICE GANG ID: ${session.peerVoiceGangId}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = VoiceGangSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Controls Bar
            if (session.callState == CallState.INCOMING_CALL) {
                // Incoming Call: Reject and Accept Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            callManager.endCall()
                            onCallTerminated()
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(VoiceGangAccentRed)
                            .testTag("call_reject_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    IconButton(
                        onClick = {
                            callManager.acceptCall()
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(VoiceGangAccentGreen)
                            .testTag("call_accept_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            } else {
                // Active or Outgoing Call Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute
                    IconButton(
                        onClick = { callManager.toggleMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (session.isMuted) VoiceGangAccentRed else VoiceGangSurfaceVariant)
                            .testTag("call_toggle_mute")
                    ) {
                        Icon(
                            imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    // Speaker
                    IconButton(
                        onClick = { callManager.toggleSpeaker() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (session.isSpeakerOn) VoiceGangPrimary else VoiceGangSurfaceVariant)
                            .testTag("call_toggle_speaker")
                    ) {
                        Icon(
                            imageVector = if (session.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Speaker",
                            tint = Color.White
                        )
                    }

                    // Switch Camera (if video)
                    if (session.isVideo) {
                        IconButton(
                            onClick = { callManager.switchCamera() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(VoiceGangSurfaceVariant)
                                .testTag("call_switch_camera")
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                        }
                    }

                    // End Call
                    IconButton(
                        onClick = {
                            callManager.endCall()
                            onCallTerminated()
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(VoiceGangAccentRed)
                            .testTag("call_end_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
