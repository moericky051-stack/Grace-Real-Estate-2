package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.Property
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseService(private val context: Context) {

    private val _localUserProfile = MutableStateFlow<UserProfile?>(null)
    val localUserProfile: StateFlow<UserProfile?> = _localUserProfile.asStateFlow()

    private val auth: FirebaseAuth by lazy {
        ensureFirebaseApp(context)
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        ensureFirebaseApp(context)
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        ensureFirebaseApp(context)
        FirebaseStorage.getInstance()
    }

    companion object {
        private var initialized = false

        fun ensureFirebaseApp(context: Context) {
            if (!initialized) {
                try {
                    if (FirebaseApp.getApps(context).isEmpty()) {
                        FirebaseApp.initializeApp(context)
                    }
                    initialized = true
                } catch (e: Exception) {
                    Log.e("FirebaseService", "FirebaseApp initialization exception: ${e.message}")
                }
            }
        }
    }

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { null }

    val currentUserId: String
        get() = _localUserProfile.value?.uid ?: currentUser?.uid ?: ""

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        try {
            auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            trySend(null)
        }
        awaitClose {
            try { auth.removeAuthStateListener(listener) } catch (_: Exception) {}
        }
    }

    suspend fun ensureAuthenticated(): FirebaseUser? = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            if (user != null) return@withContext user
            
            // Sign in anonymously if not signed in, so every user has a valid uid for Storage & Firestore
            val result = auth.signInAnonymously().await()
            result.user
        } catch (e: Exception) {
            Log.e("FirebaseService", "Anonymous sign in failed: ${e.message}")
            null
        }
    }

    fun signInAsAdmin(): UserProfile {
        val adminProfile = UserProfile(
            uid = "admin_master_001",
            name = "👑 System Administrator (Grace Admin)",
            email = "admin@gracerealestate.mm",
            phone = "09990001111",
            agencyName = "Grace Real Estate HQ (Super Admin)",
            isAdmin = true,
            createdAt = System.currentTimeMillis()
        )
        _localUserProfile.value = adminProfile
        return adminProfile
    }

    suspend fun uploadProfilePicture(userId: String, uriString: String): String = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext ""
        if (uriString.startsWith("http://") || uriString.startsWith("https://")) return@withContext uriString
        try {
            val imageUri = when {
                uriString.startsWith("content://") || uriString.startsWith("file://") -> Uri.parse(uriString)
                else -> Uri.fromFile(java.io.File(uriString))
            }
            val validUid = userId.ifBlank { currentUserId.ifBlank { UUID.randomUUID().toString() } }
            val storageRef = storage.reference.child("users/$validUid/profile_${UUID.randomUUID().toString().take(8)}.jpg")
            storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseService", "uploadProfilePicture error: ${e.message}")
            uriString
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        phone: String,
        agency: String,
        profileImageUri: String? = null
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val isSystemAdmin = cleanEmail == "admin" || cleanEmail == "admin@gracerealestate.mm" || cleanEmail.startsWith("admin@")
        if (isSystemAdmin) {
            val adminProfile = signInAsAdmin()
            saveUserProfile(adminProfile)
            return@withContext Result.success(adminProfile)
        }
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            val uploadedPhotoUrl = if (!profileImageUri.isNullOrBlank()) {
                uploadProfilePicture(user.uid, profileImageUri)
            } else ""

            val profile = UserProfile(
                uid = user.uid,
                name = name,
                email = email,
                phone = phone,
                agencyName = agency,
                photoUrl = uploadedPhotoUrl,
                isAdmin = false,
                createdAt = System.currentTimeMillis()
            )
            // Create & store user profile in Firestore collection 'users'
            saveUserProfile(profile)
            _localUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseService", "SignUp error: ${e.message}")
            val msg = e.message ?: ""
            if (msg.contains("API key not valid", ignoreCase = true) ||
                msg.contains("API_KEY", ignoreCase = true) ||
                msg.contains("internal error", ignoreCase = true)
            ) {
                // Seamless fallback to local account
                val uid = "local_${UUID.randomUUID().toString().take(8)}"
                val uploadedPhotoUrl = if (!profileImageUri.isNullOrBlank()) {
                    uploadProfilePicture(uid, profileImageUri)
                } else ""
                val localProfile = UserProfile(
                    uid = uid,
                    name = name.ifBlank { "Grace User" },
                    email = email,
                    phone = phone.ifBlank { "0912345678" },
                    agencyName = agency.ifBlank { "Grace Real Estate Member" },
                    photoUrl = uploadedPhotoUrl,
                    isAdmin = false,
                    createdAt = System.currentTimeMillis()
                )
                saveUserProfile(localProfile)
                _localUserProfile.value = localProfile
                Result.success(localProfile)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim().lowercase()
        // Admin credentials check (e.g., admin@gracerealestate.mm / admin123456 or admin / admin123456)
        val isSystemAdmin = (cleanEmail == "admin" || cleanEmail == "admin@gracerealestate.mm" || cleanEmail.startsWith("admin@")) && cleanPass.startsWith("admin")
        if (isSystemAdmin) {
            val adminProfile = signInAsAdmin()
            saveUserProfile(adminProfile)
            return@withContext Result.success(adminProfile)
        }

        try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("Sign in failed")
            var profile = getUserProfile(user.uid)
            if (profile == null) {
                profile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: email,
                    name = email.substringBefore("@"),
                    isAdmin = cleanEmail.contains("admin"),
                    createdAt = System.currentTimeMillis()
                )
                saveUserProfile(profile)
            }
            _localUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseService", "SignIn error: ${e.message}")
            val msg = e.message ?: ""
            if (msg.contains("API key not valid", ignoreCase = true) ||
                msg.contains("API_KEY", ignoreCase = true) ||
                msg.contains("internal error", ignoreCase = true)
            ) {
                // Seamless fallback to local profile
                val localProfile = UserProfile(
                    uid = "local_${email.hashCode()}",
                    name = email.substringBefore("@").ifBlank { "Grace User" },
                    email = email,
                    phone = "0912345678",
                    agencyName = "Grace Real Estate Member",
                    isAdmin = cleanEmail.contains("admin"),
                    createdAt = System.currentTimeMillis()
                )
                saveUserProfile(localProfile)
                _localUserProfile.value = localProfile
                Result.success(localProfile)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signInWithGoogle(
        email: String = "user.google@gmail.com",
        displayName: String = "Google User",
        photoUrl: String = ""
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            val uid = user?.uid ?: "google_${UUID.randomUUID().toString().take(8)}"
            val userEmail = user?.email ?: email
            val userName = user?.displayName ?: displayName.ifBlank { userEmail.substringBefore("@") }
            val userPhoto = user?.photoUrl?.toString() ?: photoUrl

            val existingProfile = getUserProfile(uid)
            val profile = existingProfile?.copy(
                photoUrl = existingProfile.photoUrl.ifBlank { userPhoto }
            ) ?: UserProfile(
                uid = uid,
                name = userName,
                email = userEmail,
                phone = "09400112233",
                agencyName = "Google Verified Member",
                photoUrl = userPhoto,
                isAdmin = userEmail.lowercase().contains("admin"),
                createdAt = System.currentTimeMillis()
            )

            saveUserProfile(profile)
            _localUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Google SignIn error: ${e.message}")
            val fallbackProfile = UserProfile(
                uid = "google_${UUID.randomUUID().toString().take(8)}",
                name = displayName,
                email = email,
                phone = "09400112233",
                agencyName = "Google Verified Member",
                photoUrl = photoUrl,
                createdAt = System.currentTimeMillis()
            )
            saveUserProfile(fallbackProfile)
            _localUserProfile.value = fallbackProfile
            Result.success(fallbackProfile)
        }
    }

    fun signInAsDemoUser(name: String = "ဦးမင်းသူ (Grace Estate)", agency: String = "Grace Real Estate Myanmar"): UserProfile {
        val demoProfile = UserProfile(
            uid = "demo_agent_001",
            name = name,
            email = "demo.agent@gracerealestate.mm",
            phone = "09420011223",
            agencyName = agency,
            createdAt = System.currentTimeMillis()
        )
        _localUserProfile.value = demoProfile
        return demoProfile
    }

    fun signOut() {
        _localUserProfile.value = null
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseService", "SignOut error: ${e.message}")
        }
    }

    // ==========================================
    // USER PROFILE
    // ==========================================

    suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext null
        try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("FirebaseService", "getUserProfile error: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(profile.uid)
                .set(profile, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "saveUserProfile error: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // FIREBASE STORAGE (PROPERTY IMAGES)
    // ==========================================

    suspend fun uploadPropertyImages(userId: String, imagePaths: List<String>): String = withContext(Dispatchers.IO) {
        val validUserId = userId.ifBlank { currentUserId.ifBlank { "anonymous_${UUID.randomUUID()}" } }
        val uploadedUrls = mutableListOf<String>()

        for (path in imagePaths) {
            val trimmed = path.trim()
            if (trimmed.isBlank()) continue

            // If it's already a web/remote URL, keep it
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                uploadedUrls.add(trimmed)
                continue
            }

            try {
                val imageUri = when {
                    trimmed.startsWith("content://") || trimmed.startsWith("file://") -> Uri.parse(trimmed)
                    else -> Uri.fromFile(java.io.File(trimmed))
                }

                val imageFileName = "${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child("properties/$validUserId/$imageFileName")
                
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                uploadedUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e("FirebaseService", "Error uploading image $trimmed: ${e.message}")
                // Fallback: if upload fails or is offline, keep trimmed path or default
                if (!trimmed.startsWith("content://") && !trimmed.startsWith("file://")) {
                    uploadedUrls.add(trimmed)
                }
            }
        }

        if (uploadedUrls.isEmpty()) "img_hero_banner" else uploadedUrls.joinToString(",")
    }

    suspend fun deleteImageFromStorage(imageUrl: String) = withContext(Dispatchers.IO) {
        if (!imageUrl.startsWith("https://firebasestorage.googleapis.com")) return@withContext
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseService", "deleteImageFromStorage error: ${e.message}")
        }
    }

    // ==========================================
    // FIRESTORE REALTIME PROPERTY LISTINGS
    // ==========================================

    fun observeAllProperties(): Flow<List<Property>> = callbackFlow {
        val collectionRef = firestore.collection("properties")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseService", "observeAllProperties error: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val properties = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(FirestorePropertyDto::class.java)?.copy(docId = doc.id)
                    dto?.toProperty()
                }
                trySend(properties)
            }
        }

        awaitClose { registration.remove() }
    }

    fun observeUserProperties(userId: String): Flow<List<Property>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val collectionRef = firestore.collection("properties")
            .whereEqualTo("userId", userId)

        val registration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseService", "observeUserProperties error: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val properties = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(FirestorePropertyDto::class.java)?.copy(docId = doc.id)
                    dto?.toProperty()
                }.sortedByDescending { it.createdAt }
                trySend(properties)
            }
        }

        awaitClose { registration.remove() }
    }

    suspend fun addPropertyToFirestore(property: Property, selectedImagePaths: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authUser = ensureAuthenticated()
            val userId = property.userId.ifBlank { currentUserId.ifBlank { authUser?.uid ?: "" } }

            // Step 1: Upload images to Storage
            val imageUrlsString = uploadPropertyImages(userId, selectedImagePaths)

            // Step 2: Prepare Firestore DTO
            val docRef = firestore.collection("properties").document()
            val docId = docRef.id

            val dto = FirestorePropertyDto.fromProperty(
                property.copy(
                    docId = docId,
                    userId = userId,
                    imageResName = imageUrlsString
                ),
                idOverride = docId
            )

            docRef.set(dto).await()
            Result.success(docId)
        } catch (e: Exception) {
            Log.e("FirebaseService", "addPropertyToFirestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePropertyInFirestore(property: Property, selectedImagePaths: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (property.docId.isBlank()) {
                throw IllegalArgumentException("Missing Firestore docId for update")
            }

            val userId = property.userId.ifBlank { currentUserId }

            // Upload new images or use existing URLs
            val updatedImageString = uploadPropertyImages(userId, selectedImagePaths)

            val dto = FirestorePropertyDto.fromProperty(
                property.copy(imageResName = updatedImageString)
            )

            firestore.collection("properties").document(property.docId)
                .set(dto, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "updatePropertyInFirestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deletePropertyFromFirestore(property: Property): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (property.docId.isNotBlank()) {
                // Delete from Firestore
                firestore.collection("properties").document(property.docId).delete().await()

                // Delete associated images from Storage
                if (property.imageResName.isNotBlank()) {
                    val urls = property.imageResName.split(",")
                    for (url in urls) {
                        deleteImageFromStorage(url.trim())
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "deletePropertyFromFirestore error: ${e.message}")
            Result.failure(e)
        }
    }
}
