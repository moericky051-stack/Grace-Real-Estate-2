package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.db.PropertyDao
import com.example.data.firebase.FirebaseService
import com.example.data.firebase.UserProfile
import com.example.data.model.Property
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.combine

class PropertyRepository(
    private val propertyDao: PropertyDao,
    context: Context
) {

    val firebaseService = FirebaseService(context)

    // Primary properties flow: prioritizes live Firestore updates with Room DB offline fallback & favorite state mapping
    val allProperties: Flow<List<Property>> = combine(
        firebaseService.observeAllProperties(),
        propertyDao.getAllProperties()
    ) { firestoreList, roomList ->
        if (firestoreList.isNotEmpty()) {
            val favDocIds = roomList.filter { it.isFavorite && it.docId.isNotBlank() }.map { it.docId }.toSet()
            val favTitles = roomList.filter { it.isFavorite && it.docId.isBlank() }.map { "${it.title}_${it.agentPhone}" }.toSet()

            firestoreList.map { prop ->
                val isFav = (prop.docId.isNotBlank() && favDocIds.contains(prop.docId)) ||
                        favTitles.contains("${prop.title}_${prop.agentPhone}")
                val localMatch = roomList.firstOrNull { 
                    (it.docId.isNotBlank() && it.docId == prop.docId) ||
                    (it.title == prop.title && it.agentPhone == prop.agentPhone)
                }
                prop.copy(
                    id = localMatch?.id ?: prop.id,
                    isFavorite = isFav
                )
            }
        } else {
            roomList
        }
    }

    val favoriteProperties: Flow<List<Property>> = propertyDao.getFavoriteProperties()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val externalScope = CoroutineScope(Dispatchers.IO)

    init {
        // Start listening to Auth state & Local Profile
        externalScope.launch {
            combine(
                firebaseService.observeAuthState(),
                firebaseService.localUserProfile
            ) { firebaseUser, localProfile ->
                if (localProfile != null) {
                    localProfile
                } else if (firebaseUser != null) {
                    val profile = firebaseService.getUserProfile(firebaseUser.uid)
                    profile ?: UserProfile(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: if (firebaseUser.isAnonymous) "Guest User" else "User",
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    null
                }
            }.collectLatest { profile ->
                _userProfile.value = profile
            }
        }

        // Realtime Firestore Listener to keep local Room DB updated as offline cache
        externalScope.launch {
            try {
                firebaseService.observeAllProperties().collectLatest { firestoreList ->
                    if (firestoreList.isNotEmpty()) {
                        val firestoreDocIds = firestoreList.map { it.docId }.filter { it.isNotBlank() }.toSet()

                        for (prop in firestoreList) {
                            val existing = if (prop.docId.isNotBlank()) {
                                propertyDao.getPropertyByDocId(prop.docId)
                            } else {
                                propertyDao.getPropertyByTitleAndPhone(prop.title, prop.agentPhone)
                            }
                            if (existing != null) {
                                propertyDao.insertProperty(
                                    prop.copy(
                                        id = existing.id,
                                        isFavorite = existing.isFavorite
                                    )
                                )
                            } else {
                                propertyDao.insertProperty(prop)
                            }
                        }

                        // Purge items from Room cache if they were deleted on Firestore
                        val localProperties = propertyDao.getAllPropertiesList()
                        for (localProp in localProperties) {
                            if (localProp.docId.isNotBlank() && !firestoreDocIds.contains(localProp.docId)) {
                                propertyDao.deleteProperty(localProp.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PropertyRepository", "Firestore realtime listener error: ${e.message}")
            }
        }
    }

    fun getPropertyById(id: Long): Flow<Property?> = propertyDao.getPropertyById(id)

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        propertyDao.updateFavorite(id, !currentStatus)
    }

    suspend fun insertPropertyWithFirebase(
        property: Property,
        selectedImagePaths: List<String>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Upload images to Storage & save doc to Firestore
            val firestoreResult = firebaseService.addPropertyToFirestore(property, selectedImagePaths)
            val docId = firestoreResult.getOrDefault("")

            // Insert locally in Room DB
            val finalProp = property.copy(
                docId = docId,
                userId = firebaseService.currentUserId,
                imageResName = if (selectedImagePaths.isNotEmpty()) selectedImagePaths.joinToString(",") else property.imageResName
            )
            val localId = propertyDao.insertProperty(finalProp)

            Result.success(localId)
        } catch (e: Exception) {
            Log.e("PropertyRepository", "insertPropertyWithFirebase error: ${e.message}")
            // Fallback: save locally
            val localId = propertyDao.insertProperty(property)
            Result.success(localId)
        }
    }

    suspend fun updatePropertyWithFirebase(
        property: Property,
        selectedImagePaths: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (property.docId.isNotBlank()) {
                firebaseService.updatePropertyInFirestore(property, selectedImagePaths)
            }
            propertyDao.insertProperty(property)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PropertyRepository", "updatePropertyWithFirebase error: ${e.message}")
            propertyDao.insertProperty(property)
            Result.success(Unit)
        }
    }

    suspend fun deletePropertyWithFirebase(property: Property): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (property.docId.isNotBlank()) {
                firebaseService.deletePropertyFromFirestore(property)
            }
            if (property.id > 0) {
                propertyDao.deleteProperty(property.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PropertyRepository", "deletePropertyWithFirebase error: ${e.message}")
            if (property.id > 0) {
                propertyDao.deleteProperty(property.id)
            }
            Result.success(Unit)
        }
    }

    suspend fun checkAndSeedInitialData() {
        if (propertyDao.getPropertyCount() == 0) {
            val sampleProperties = listOf(
                Property(
                    title = "ကမာရွတ်မြို့နယ် ပြင်ဆင်ပြီး ကွန်ဒိုသစ် အမြန်ရောင်းမည်",
                    listingType = "BUY",
                    propertyType = "Condo",
                    priceLakhs = 3800.0,
                    pricePeriod = "TOTAL",
                    city = "Yangon",
                    township = "ကမာရွတ် (Kamayut)",
                    address = "လှိုင်မြစ်လမ်းမအနီး၊ ကမာရွတ်မြို့နယ်၊ ရန်ကုန်။",
                    areaSqft = 1450,
                    bedrooms = 3,
                    bathrooms = 2,
                    floorLevel = "8th Floor",
                    furnishing = "Fully Furnished",
                    deedType = "Grant Land (ဂရန်အမည်ပေါက်)",
                    description = "လှိုင်မြစ်မြင်ကွင်းရ ကွန်ဒိုအခန်းကျယ်။ Master Bedroom ၁ ခန်း၊ Single Bedroom ၂ ခန်း ပါဝင်သည်။ Lift, 24hr Security, Backup Generator, Swimming Pool ပါဝင်ပြီး အသင့်နေထိုင်နိုင်ပါသည်။",
                    imageResName = "img_hero_banner",
                    agentName = "ဦးမင်းသူ (Grace Estate)",
                    agentPhone = "09420011223",
                    agentType = "Verified Agent",
                    isFavorite = true
                ),
                Property(
                    title = "ဗဟန်းမြို့နယ် ဆိတ်ငြိမ်ရပ်ကွက် လုံးချင်း 2RC အိမ်ကျယ် ရောင်းမည်",
                    listingType = "BUY",
                    propertyType = "House",
                    priceLakhs = 18500.0,
                    pricePeriod = "TOTAL",
                    city = "Yangon",
                    township = "ဗဟန်း (Bahan)",
                    address = "ဆရာစံလမ်းသွယ်၊ ဗဟန်းမြို့နယ်၊ ရန်ကုန်။",
                    areaSqft = 3600,
                    bedrooms = 4,
                    bathrooms = 4,
                    floorLevel = "2-Story Villa",
                    furnishing = "Semi-Furnished",
                    deedType = "Ancestral Land (ဘိုးဘွားပိုင်မြေ)",
                    description = "ရွှေတိဂုံဘုရားအနီး ဆိတ်ငြိမ်ရပ်ကွက်ရှိ လုံးချင်းနှစ်ထပ်တိုက်။ ကား ၄ စီး ရပ်နားရန် နေရာကျယ် ပါဝင်ပြီး သစ်ပင်ရိပ် ဝန်းကျင်ကောင်းမွန်ပါသည်။",
                    imageResName = "img_property_villa",
                    agentName = "ဒေါ်နန်းမိုး (Yangon Luxury Realty)",
                    agentPhone = "09790099887",
                    agentType = "Exclusive Agent",
                    isFavorite = false
                ),
                Property(
                    title = "ရန်ကင်းမြို့နယ် ဆိုင်ခန်း သို့မဟုတ် ရုံးခန်းဌားရန်",
                    listingType = "RENT",
                    propertyType = "Commercial",
                    priceLakhs = 35.0,
                    pricePeriod = "PER_MONTH",
                    city = "Yangon",
                    township = "ရန်ကင်း (Yankin)",
                    address = "ကံဘဲ့လမ်းမကြီးပေါ်၊ ရန်ကင်းမြို့နယ်၊ ရန်ကုန်။",
                    areaSqft = 1200,
                    bedrooms = 1,
                    bathrooms = 2,
                    floorLevel = "Ground Floor",
                    furnishing = "Unfurnished",
                    deedType = "Commercial Lease",
                    description = "လူစည်ကားသော လမ်းမကြီးပေါ်တွင် တည်ရှိပြီး Showroom, Clinic, သို့မဟုတ် Office ခန်းမဖွင့်လှစ်ရန် လွန်စွာ သင့်တော်ပါသည်။ ကားပါကင် နေရာကျယ်ဝန်းသည်။",
                    imageResName = "img_hero_banner",
                    agentName = "ကိုအောင်ကျော် (City Property Agency)",
                    agentPhone = "09250123456",
                    agentType = "Verified Agent",
                    isFavorite = false
                )
            )
            propertyDao.insertProperties(sampleProperties)
        }
    }
}
