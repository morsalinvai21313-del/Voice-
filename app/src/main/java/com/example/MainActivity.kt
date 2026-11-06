package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.models.Group
import com.example.models.UserProfile
import com.example.repository.VoiceGangRepository
import com.example.ui.screens.*
import com.example.ui.theme.VoiceGangPrimary
import com.example.ui.theme.VoiceGangTheme
import com.example.webrtc.VoiceGangCallManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    CHATS,
    CALLS,
    GROUPS,
    PROFILE
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            VoiceGangTheme(darkTheme = isDarkTheme) {
                VoiceGangApp(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = it }
                )
            }
        }
    }
}

@Composable
fun VoiceGangApp(
    isDarkTheme: Boolean = true,
    onToggleDarkTheme: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val repository = remember { VoiceGangRepository(context) }
    val callManager = remember { VoiceGangCallManager(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    // Listen to Firebase auth state
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Load or generate user profile with permanent 8-digit ID when authenticated
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            try {
                val profile = repository.getOrCreateUserProfile(
                    displayNameFallback = user.displayName ?: "Voice Gangster",
                    emailFallback = user.email ?: "user@voicegang.com"
                )
                userProfile = profile
            } catch (e: Exception) {
                userProfile = UserProfile(
                    userId = user.uid,
                    voiceGangId = "83749261",
                    displayName = user.displayName ?: "Voice Gangster",
                    bio = "Voice Gang Member"
                )
            }
        } else {
            userProfile = null
        }
    }

    // Observe real-time incoming call signaling
    val incomingCallRecord by remember(currentUser) {
        if (currentUser != null) repository.observeIncomingCalls()
        else kotlinx.coroutines.flow.flowOf(null)
    }.collectAsState(initial = null)

    LaunchedEffect(incomingCallRecord) {
        val call = incomingCallRecord
        if (call != null && callManager.currentSession.value == null) {
            callManager.onIncomingCall(
                callId = call.id,
                callerUid = call.callerId,
                callerName = call.callerName,
                callerVoiceGangId = call.callerVoiceGangId,
                isVideo = call.callType == "video"
            )
        }
    }

    val activeCallSession by callManager.currentSession.collectAsState()

    if (currentUser == null) {
        VoiceGangAuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        // Authenticated Main Experience
        var currentTab by remember { mutableStateOf(MainTab.HOME) }
        var activeConversationData by remember { mutableStateOf<Triple<String, String, String>?>(null) } // (id, name, gangId)
        var activePeerUid by remember { mutableStateOf("") }
        var activeGroupRoom by remember { mutableStateOf<Group?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.HOME,
                            onClick = {
                                activeConversationData = null
                                activeGroupRoom = null
                                currentTab = MainTab.HOME
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.CHATS,
                            onClick = {
                                activeConversationData = null
                                activeGroupRoom = null
                                currentTab = MainTab.CHATS
                            },
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                            label = { Text("Chats", fontWeight = if (currentTab == MainTab.CHATS) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_chats")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.CALLS,
                            onClick = {
                                activeConversationData = null
                                activeGroupRoom = null
                                currentTab = MainTab.CALLS
                            },
                            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                            label = { Text("Calls", fontWeight = if (currentTab == MainTab.CALLS) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_calls")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.GROUPS,
                            onClick = {
                                activeConversationData = null
                                activeGroupRoom = null
                                currentTab = MainTab.GROUPS
                            },
                            icon = { Icon(Icons.Default.Groups, contentDescription = "Groups") },
                            label = { Text("Groups", fontWeight = if (currentTab == MainTab.GROUPS) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_groups")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.PROFILE,
                            onClick = {
                                activeConversationData = null
                                activeGroupRoom = null
                                currentTab = MainTab.PROFILE
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontWeight = if (currentTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_profile")
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when {
                        activeConversationData != null -> {
                            val (convId, peerName, peerGangId) = activeConversationData!!
                            ConversationDetailScreen(
                                conversationId = convId,
                                peerName = peerName,
                                peerGangId = peerGangId,
                                peerUid = activePeerUid,
                                currentUserId = currentUser!!.uid,
                                myName = userProfile?.displayName ?: "Voice Gangster",
                                repository = repository,
                                onBack = { activeConversationData = null },
                                onStartCall = { targetUid, targetName, targetGangId, isVideo ->
                                    coroutineScope.launch {
                                        val callId = repository.initiateCall(
                                            receiverUid = targetUid,
                                            receiverName = targetName,
                                            receiverVoiceGangId = targetGangId,
                                            callerName = userProfile?.displayName ?: "Voice Gangster",
                                            callerVoiceGangId = userProfile?.voiceGangId ?: "",
                                            isVideo = isVideo
                                        )
                                        callManager.startOutgoingCall(
                                            callId = callId,
                                            peerUid = targetUid,
                                            peerName = targetName,
                                            peerVoiceGangId = targetGangId,
                                            isVideo = isVideo
                                        )
                                    }
                                }
                            )
                        }

                        activeGroupRoom != null -> {
                            GroupVoiceRoomScreen(
                                group = activeGroupRoom!!,
                                currentUserId = currentUser!!.uid,
                                repository = repository,
                                onBack = { activeGroupRoom = null }
                            )
                        }

                        else -> {
                            when (currentTab) {
                                MainTab.HOME -> HomeScreen(
                                    myProfile = userProfile,
                                    repository = repository,
                                    onOpenChat = { targetUid, targetName, targetGangId ->
                                        coroutineScope.launch {
                                            val convId = repository.getOrCreateConversation(
                                                targetUid = targetUid,
                                                targetName = targetName,
                                                targetGangId = targetGangId,
                                                myName = userProfile?.displayName ?: "Voice Gangster",
                                                myGangId = userProfile?.voiceGangId ?: ""
                                            )
                                            activePeerUid = targetUid
                                            activeConversationData = Triple(convId, targetName, targetGangId)
                                        }
                                    },
                                    onStartCall = { targetUid, targetName, targetGangId, isVideo ->
                                        coroutineScope.launch {
                                            val callId = repository.initiateCall(
                                                receiverUid = targetUid,
                                                receiverName = targetName,
                                                receiverVoiceGangId = targetGangId,
                                                callerName = userProfile?.displayName ?: "Voice Gangster",
                                                callerVoiceGangId = userProfile?.voiceGangId ?: "",
                                                isVideo = isVideo
                                            )
                                            callManager.startOutgoingCall(
                                                callId = callId,
                                                peerUid = targetUid,
                                                peerName = targetName,
                                                peerVoiceGangId = targetGangId,
                                                isVideo = isVideo
                                            )
                                        }
                                    },
                                    onJoinGroupRoom = { group ->
                                        activeGroupRoom = group
                                    }
                                )

                                MainTab.CHATS -> ChatsScreen(
                                    currentUserId = currentUser!!.uid,
                                    repository = repository,
                                    onOpenConversation = { convId, peerName, peerGangId, peerUid ->
                                        activePeerUid = peerUid
                                        activeConversationData = Triple(convId, peerName, peerGangId)
                                    },
                                    onSearchPeople = {
                                        currentTab = MainTab.HOME
                                    }
                                )

                                MainTab.CALLS -> CallsScreen(
                                    currentUserId = currentUser!!.uid,
                                    repository = repository,
                                    onCallAgain = { targetUid, targetName, targetGangId, isVideo ->
                                        coroutineScope.launch {
                                            val callId = repository.initiateCall(
                                                receiverUid = targetUid,
                                                receiverName = targetName,
                                                receiverVoiceGangId = targetGangId,
                                                callerName = userProfile?.displayName ?: "Voice Gangster",
                                                callerVoiceGangId = userProfile?.voiceGangId ?: "",
                                                isVideo = isVideo
                                            )
                                            callManager.startOutgoingCall(
                                                callId = callId,
                                                peerUid = targetUid,
                                                peerName = targetName,
                                                peerVoiceGangId = targetGangId,
                                                isVideo = isVideo
                                            )
                                        }
                                    }
                                )

                                MainTab.GROUPS -> GroupsScreen(
                                    currentUserId = currentUser!!.uid,
                                    repository = repository,
                                    onOpenGroupRoom = { group ->
                                        activeGroupRoom = group
                                    }
                                )

                                MainTab.PROFILE -> ProfileScreen(
                                    profile = userProfile,
                                    repository = repository,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme,
                                    onSignOut = {
                                        Firebase.auth.signOut()
                                        coroutineScope.launch {
                                            try {
                                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                            } catch (ignored: Exception) {}
                                            currentUser = null
                                            userProfile = null
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Full-Screen WebRTC Call Overlay (Audio / Video)
            if (activeCallSession != null) {
                WebRtcCallOverlayScreen(
                    session = activeCallSession!!,
                    callManager = callManager,
                    onCallTerminated = {
                        val session = activeCallSession
                        if (session != null && session.callId.isNotEmpty()) {
                            coroutineScope.launch {
                                repository.updateCallStatus(
                                    session.callId,
                                    "ended",
                                    session.durationSeconds
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
