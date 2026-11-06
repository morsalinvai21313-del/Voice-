package com.example.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val voiceGangId: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class VoiceGangIdDirectory(
    @DocumentId val id: String = "",
    val voiceGangId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val participantUids: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantGangIds: Map<String, String> = emptyMap(),
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp val lastMessageTime: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val type: String = "text", // "text", "voice", "image", "call_log"
    val mediaUrl: String = "",
    val durationSeconds: Double = 0.0,
    val replyToMessageId: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val avatarUrl: String = "",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    val adminUids: List<String> = emptyList(),
    val activeRoomId: String = "",
    val isVoiceActive: Boolean = false,
    val activeSpeakers: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CallRecord(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerVoiceGangId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverVoiceGangId: String = "",
    val callType: String = "audio", // "audio", "video", "group_audio", "group_video"
    val status: String = "calling", // "calling", "ringing", "connected", "ended", "rejected", "missed"
    val offerSdp: String = "",
    val answerSdp: String = "",
    val durationSeconds: Long = 0L,
    val isGroup: Boolean = false,
    val groupId: String = "",
    val activeParticipants: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
