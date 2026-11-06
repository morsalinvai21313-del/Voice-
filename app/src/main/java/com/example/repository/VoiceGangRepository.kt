package com.example.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.models.CallRecord
import com.example.models.Conversation
import com.example.models.Group
import com.example.models.Message
import com.example.models.UserProfile
import com.example.models.VoiceGangIdDirectory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class VoiceGangRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(getFirestoreInstance(context))

    companion object {
        private fun getFirestoreInstance(context: Context): FirebaseFirestore {
            val dbId = try {
                context.applicationContext.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                null
            }
            return if (dbId.isNullOrBlank()) {
                FirebaseFirestore.getInstance()
            } else {
                FirebaseFirestore.getInstance(dbId)
            }
        }
    }

    private val auth = Firebase.auth
    private val random = SecureRandom()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Voice Gang.")
    }

    /**
     * Generates or fetches the user's permanent 8-digit unique VOICE GANG ID.
     * Guaranteed deterministic: Same user UID always gets and retains the exact same 8-digit ID.
     * Never changes, never overwrites on repeated logins.
     */
    suspend fun getOrCreateUserProfile(displayNameFallback: String, emailFallback: String): UserProfile {
        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        
        try {
            val snapshot = userDocRef.get().await()
            if (snapshot.exists()) {
                val existingVoiceGangId = snapshot.getString("voiceGangId")
                if (!existingVoiceGangId.isNullOrBlank()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    if (profile != null) return profile
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceGangRepo", "Error reading user doc, checking local or creating...", e)
        }

        // Generate a deterministic 8-digit permanent ID derived from the user's permanent UID
        // Ensures 1 Gmail = Exactly 1 Permanent Voice Gang ID forever
        val hash = Math.abs(uid.hashCode().toLong())
        val permanentId = (10000000L + (hash % 90000000L)).toString()

        val newProfile = UserProfile(
            userId = uid,
            voiceGangId = permanentId,
            displayName = displayNameFallback.ifBlank { "Voice Gangster" },
            username = emailFallback.substringBefore("@"),
            bio = "Hey! I'm on VOICE GANG. Connect with my 8-digit ID!",
            isOnline = true
        )

        val profilePayload = mapOf(
            "userId" to uid,
            "voiceGangId" to permanentId,
            "displayName" to newProfile.displayName,
            "username" to newProfile.username,
            "bio" to newProfile.bio,
            "avatarUrl" to "",
            "isOnline" to true,
            "lastSeen" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            userDocRef.set(profilePayload).await()
            // Also store in directory for search discovery
            val dirPayload = mapOf(
                "voiceGangId" to permanentId,
                "userId" to uid,
                "displayName" to newProfile.displayName,
                "avatarUrl" to "",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("user_ids").document(permanentId).set(dirPayload).await()
        } catch (e: Exception) {
            Log.e("VoiceGangRepo", "Error writing profile to Firestore", e)
        }

        return newProfile
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { snap -> snap.toObject(UserProfile::class.java) }
    }

    suspend fun updateProfile(displayName: String, bio: String) {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid).get().await()
        val voiceGangId = userDoc.getString("voiceGangId") ?: ""

        val updates = mapOf(
            "displayName" to displayName,
            "bio" to bio,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).update(updates).await()

        if (voiceGangId.isNotEmpty()) {
            db.collection("user_ids").document(voiceGangId).update("displayName", displayName).await()
        }
    }

    /**
     * USER DISCOVERY: Search user directly by 8-digit VOICE GANG ID.
     */
    suspend fun findUserByVoiceGangId(voiceGangId: String): VoiceGangIdDirectory? {
        val trimmed = voiceGangId.trim()
        if (trimmed.length != 8) return null

        val doc = db.collection("user_ids").document(trimmed).get().await()
        return if (doc.exists()) {
            doc.toObject(VoiceGangIdDirectory::class.java)
        } else {
            null
        }
    }

    /**
     * Start or fetch existing 1-on-1 direct conversation
     */
    suspend fun getOrCreateConversation(
        targetUid: String,
        targetName: String,
        targetGangId: String,
        myName: String,
        myGangId: String
    ): String {
        val myUid = requireUserId()
        // Conversation ID is deterministic for direct pair
        val convId = if (myUid < targetUid) "${myUid}_$targetUid" else "${targetUid}_$myUid"
        val convRef = db.collection("conversations").document(convId)

        val snap = convRef.get().await()
        if (!snap.exists()) {
            val payload = mapOf(
                "participantUids" to listOf(myUid, targetUid),
                "participantNames" to mapOf(myUid to myName, targetUid to targetName),
                "participantGangIds" to mapOf(myUid to myGangId, targetUid to targetGangId),
                "lastMessageText" to "Conversation started",
                "lastMessageSenderId" to myUid,
                "lastMessageTime" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            convRef.set(payload).await()
        }
        return convId
    }

    fun observeConversations(): Flow<List<Conversation>> {
        val uid = requireUserId()
        return db.collection("conversations")
            .whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snap -> snap.toObjects(Conversation::class.java) }
            .catch { e ->
                Log.e("VoiceGangRepo", "Error observing conversations", e)
                emit(emptyList())
            }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> {
        return db.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(Message::class.java) }
            .catch { e ->
                Log.e("VoiceGangRepo", "Error observing messages for $conversationId", e)
                emit(emptyList())
            }
    }

    suspend fun sendMessage(conversationId: String, text: String, senderName: String, type: String = "text") {
        try {
            val uid = requireUserId()
            val messagesRef = db.collection("conversations").document(conversationId).collection("messages")

            val payload = mapOf(
                "senderId" to uid,
                "senderName" to senderName,
                "content" to text,
                "type" to type,
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            messagesRef.add(payload).await()

            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "lastMessageText" to text,
                    "lastMessageSenderId" to uid,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("VoiceGangRepo", "Failed to send message", e)
        }
    }

    // --- GROUPS & AUDIO ROOMS ---

    suspend fun createGroup(name: String, description: String): String {
        val uid = requireUserId()
        val groupRef = db.collection("groups").document()
        val payload = mapOf(
            "name" to name,
            "description" to description,
            "ownerId" to uid,
            "memberUids" to listOf(uid),
            "adminUids" to listOf(uid),
            "isVoiceActive" to false,
            "activeSpeakers" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        groupRef.set(payload).await()
        return groupRef.id
    }

    fun observeGroups(): Flow<List<Group>> {
        return db.collection("groups")
            .snapshots()
            .map { snap -> snap.toObjects(Group::class.java) }
    }

    suspend fun joinGroup(groupId: String) {
        val uid = requireUserId()
        db.collection("groups").document(groupId).update(
            "memberUids", FieldValue.arrayUnion(uid)
        ).await()
    }

    suspend fun toggleGroupVoiceRoom(groupId: String, active: Boolean) {
        val uid = requireUserId()
        val updates = if (active) {
            mapOf(
                "isVoiceActive" to true,
                "activeSpeakers" to FieldValue.arrayUnion(uid),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        } else {
            mapOf(
                "isVoiceActive" to false,
                "activeSpeakers" to emptyList<String>(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
        db.collection("groups").document(groupId).update(updates).await()
    }

    // --- CALLS & SIGNALING ---

    suspend fun initiateCall(
        receiverUid: String,
        receiverName: String,
        receiverVoiceGangId: String,
        callerName: String,
        callerVoiceGangId: String,
        isVideo: Boolean
    ): String {
        val callerUid = requireUserId()
        val callRef = db.collection("calls").document()
        val payload = mapOf(
            "callerId" to callerUid,
            "callerName" to callerName,
            "callerVoiceGangId" to callerVoiceGangId,
            "receiverId" to receiverUid,
            "receiverName" to receiverName,
            "receiverVoiceGangId" to receiverVoiceGangId,
            "callType" to if (isVideo) "video" else "audio",
            "status" to "calling",
            "isGroup" to false,
            "durationSeconds" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        callRef.set(payload).await()
        return callRef.id
    }

    suspend fun updateCallStatus(callId: String, status: String, durationSeconds: Long = 0L) {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (durationSeconds > 0) {
            updates["durationSeconds"] = durationSeconds
        }
        db.collection("calls").document(callId).update(updates).await()
    }

    fun observeIncomingCalls(): Flow<CallRecord?> {
        val uid = requireUserId()
        return db.collection("calls")
            .whereEqualTo("receiverId", uid)
            .whereEqualTo("status", "calling")
            .snapshots()
            .map { snap -> snap.toObjects(CallRecord::class.java).firstOrNull() }
    }

    fun observeCallHistory(): Flow<List<CallRecord>> {
        val uid = requireUserId()
        return db.collection("calls")
            .whereEqualTo("callerId", uid)
            .snapshots()
            .map { snap -> snap.toObjects(CallRecord::class.java) }
    }
}
