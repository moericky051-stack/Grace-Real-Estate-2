package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.UserProfile
import com.example.data.model.Property
import com.example.data.repository.PropertyRepository
import com.example.ui.theme.AppThemeOption
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RealEstateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository

    val searchQuery = MutableStateFlow("")
    val selectedListingTypeTab = MutableStateFlow("ALL") // "ALL", "BUY", "RENT", "MY_LISTINGS"
    val selectedCity = MutableStateFlow("ALL") // "ALL", "Yangon", "Mandalay", "Naypyidaw", "Pyin Oo Lwin", "Taunggyi"
    val selectedPropertyType = MutableStateFlow("ALL") // "ALL", "Condo", "Apartment", "House", "Land", "Commercial"
    val maxPriceLakhs = MutableStateFlow(20000f) // Max price limit
    val minBedrooms = MutableStateFlow(0) // 0 = Any
    val isFilterSheetOpen = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)
    val loadingMessage = MutableStateFlow("လုပ်ဆောင်နေပါသည်...")
    val selectedTheme = MutableStateFlow(AppThemeOption.NAVY_GOLD)

    val userProfile: StateFlow<UserProfile?>

    fun setTheme(theme: AppThemeOption) {
        selectedTheme.value = theme
    }

    init {
        val db = AppDatabase.getInstance(application)
        repository = PropertyRepository(db.propertyDao(), application)
        userProfile = repository.userProfile

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun currentUserId(): String = userProfile.value?.uid ?: repository.firebaseService.currentUserId

    fun isAdmin(): Boolean {
        val profile = userProfile.value
        if (profile != null) {
            if (profile.isAdmin) return true
            val email = profile.email.lowercase()
            if (email == "admin" || email.startsWith("admin@") || profile.uid == "admin_master_001") return true
        }
        return false
    }

    fun isUserSignedIn(): Boolean {
        val profile = userProfile.value
        if (profile != null && profile.uid.isNotBlank()) return true
        val user = repository.firebaseService.currentUser
        return user != null && !user.isAnonymous
    }

    // Auth actions
    fun signUp(
        email: String,
        pass: String,
        name: String,
        phone: String,
        agency: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            loadingMessage.value = "အကောင့် သစ်ပြုလုပ်နေပါသည်..."
            val result = repository.firebaseService.signUpWithEmail(email, pass, name, phone, agency)
            isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "အကောင့် ပြုလုပ်ခြင်း မအောင်မြင်ပါ"
                onError(err)
            }
        }
    }

    fun signIn(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            loadingMessage.value = "အကောင့် ဝင်ရောက်နေပါသည်..."
            val result = repository.firebaseService.signInWithEmail(email, pass)
            isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "အကောင့် ဝင်ရောက်ခြင်း မအောင်မြင်ပါ"
                onError(err)
            }
        }
    }

    fun signInAsDemoUser(onSuccess: () -> Unit) {
        repository.firebaseService.signInAsDemoUser()
        onSuccess()
    }

    fun signInAsAdmin(onSuccess: () -> Unit) {
        repository.firebaseService.signInAsAdmin()
        onSuccess()
    }

    fun signOut() {
        repository.firebaseService.signOut()
    }

    fun updateProfileInfo(
        name: String,
        phone: String,
        agencyName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val uid = repository.firebaseService.currentUserId
            if (uid.isBlank()) {
                onError("အကောင့်ဝင်ရောက်ရန် လိုအပ်ပါသည်")
                return@launch
            }
            isLoading.value = true
            loadingMessage.value = "ကိုယ်ရေးအချက်အလက် ပြင်ဆင်နေပါသည်..."
            val updated = UserProfile(
                uid = uid,
                name = name,
                email = repository.firebaseService.currentUser?.email ?: "",
                phone = phone,
                agencyName = agencyName
            )
            val res = repository.firebaseService.saveUserProfile(updated)
            isLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.localizedMessage ?: "ပြင်ဆင်ခြင်း မအောင်မြင်ပါ")
            }
        }
    }

    fun refreshCloudData(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            isSyncing.value = true
            // Realtime listener is automatically active
            isSyncing.value = false
            onComplete?.invoke(true)
        }
    }

    val favoriteProperties: StateFlow<List<Property>> = repository.favoriteProperties
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private data class FilterParams(
        val query: String,
        val tab: String,
        val city: String,
        val propType: String,
        val maxPrice: Float,
        val currentUid: String
    )

    private val filterState = combine(
        searchQuery,
        selectedListingTypeTab,
        selectedCity,
        selectedPropertyType,
        maxPriceLakhs
    ) { query, tab, city, propType, maxPrice ->
        FilterParams(query, tab, city, propType, maxPrice, repository.firebaseService.currentUserId)
    }

    val filteredProperties: StateFlow<List<Property>> = combine(
        repository.allProperties,
        filterState,
        minBedrooms
    ) { properties, filter, beds ->
        properties.filter { p ->
            val matchesQuery = filter.query.isBlank() ||
                    p.title.contains(filter.query, ignoreCase = true) ||
                    p.township.contains(filter.query, ignoreCase = true) ||
                    p.city.contains(filter.query, ignoreCase = true) ||
                    p.address.contains(filter.query, ignoreCase = true)

            val matchesTab = when (filter.tab) {
                "BUY" -> p.listingType == "BUY"
                "RENT" -> p.listingType == "RENT"
                "MY_LISTINGS" -> (p.userId.isNotBlank() && p.userId == filter.currentUid) || p.agentType == "Direct Post" || p.agentType == "User Post"
                else -> true
            }

            val matchesCity = filter.city == "ALL" || p.city.equals(filter.city, ignoreCase = true)
            val matchesPropType = filter.propType == "ALL" || p.propertyType.equals(filter.propType, ignoreCase = true)
            val matchesPrice = p.priceLakhs <= filter.maxPrice
            val matchesBeds = beds == 0 || p.bedrooms >= beds

            matchesQuery && matchesTab && matchesCity && matchesPropType && matchesPrice && matchesBeds
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val myListings: StateFlow<List<Property>> = repository.allProperties
        .combine(repository.userProfile) { properties, profile ->
            val uid = profile?.uid ?: repository.firebaseService.currentUserId
            properties.filter { p ->
                (uid.isNotBlank() && p.userId == uid) || p.agentType == "Direct Post" || p.agentType == "User Post"
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPropertyById(id: Long): StateFlow<Property?> {
        val flow = MutableStateFlow<Property?>(null)
        viewModelScope.launch {
            repository.getPropertyById(id).collect {
                flow.value = it
            }
        }
        return flow
    }

    fun toggleFavorite(property: Property) {
        viewModelScope.launch {
            repository.toggleFavorite(property.id, property.isFavorite)
        }
    }

    fun postNewProperty(
        title: String,
        listingType: String,
        propertyType: String,
        priceLakhs: Double,
        pricePeriod: String,
        city: String,
        township: String,
        address: String,
        areaSqft: Int,
        bedrooms: Int,
        bathrooms: Int,
        floorLevel: String,
        furnishing: String,
        deedType: String,
        description: String,
        agentName: String,
        agentPhone: String,
        imageResName: String? = null,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isUserSignedIn()) {
            onError?.invoke("Guest မဖြစ်နိုင်ပါ။ ကြော်ငြာ တင်ရန် အကောင့်ဝင်ရောက်ပါ သို့မဟုတ် အကောင့်သစ်ပြုလုပ်ပါ")
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            loadingMessage.value = "Firebase Storage သို့ ဓာတ်ပုံများ တင်နေပါသည်..."

            val defaultImg = if (propertyType == "House" || propertyType == "Land") "img_property_villa" else "img_hero_banner"
            val rawImageString = if (!imageResName.isNullOrBlank()) imageResName else defaultImg
            val imageList = if (rawImageString.contains(",")) {
                rawImageString.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                listOf(rawImageString)
            }

            val currentUid = currentUserId()

            val newProperty = Property(
                userId = currentUid,
                title = title.ifBlank { "အိမ်ခြံမြေ ရောင်းရန်/ငှားရန်" },
                listingType = listingType,
                propertyType = propertyType,
                priceLakhs = priceLakhs,
                pricePeriod = pricePeriod,
                city = city,
                township = township,
                address = address,
                areaSqft = areaSqft,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                floorLevel = floorLevel,
                furnishing = furnishing,
                deedType = deedType,
                description = description,
                imageResName = rawImageString,
                agentName = agentName.ifBlank { userProfile.value?.name ?: "အိမ်ပိုင်ရှင်" },
                agentPhone = agentPhone.ifBlank { userProfile.value?.phone ?: "0912345678" },
                agentType = "User Post",
                isFavorite = false,
                status = "ACTIVE"
            )

            loadingMessage.value = "Firestore သို့ ကြော်ငြာ သိမ်းဆည်းနေပါသည်..."
            repository.insertPropertyWithFirebase(newProperty, imageList)

            isLoading.value = false
            onSuccess()
        }
    }

    fun updateProperty(
        property: Property,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val currentUid = currentUserId()
        val admin = isAdmin()
        if (!isUserSignedIn() || currentUid.isBlank()) {
            onError?.invoke("Guest မဖြစ်နိုင်ပါ။ ကြော်ငြာ ပြင်ဆင်ရန် အကောင့်ဝင်ရောက်ပါ")
            return
        }
        val isAgencyListing = property.agentType != "User Post" && property.agentType != "Direct Post"
        val isOwner = (property.userId.isNotBlank() && property.userId == currentUid) ||
                (property.userId.isBlank() && (property.agentType == "Direct Post" || property.agentType == "User Post"))
        val canModify = if (isAgencyListing) admin else (isOwner || admin)

        if (!canModify) {
            val errMsg = if (isAgencyListing) "အကျိုးဆောင် ကြော်ငြာများကို Admin တစ်ဦးတည်းသာ ပြင်ဆင်ခွင့် ရှိပါသည်"
                         else "မိမိ ပိုင်ဆိုင်သော ကြော်ငြာများ သို့မဟုတ် Admin သာလျှင် ပြင်ဆင်ခွင့် ရှိပါသည်"
            onError?.invoke(errMsg)
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            loadingMessage.value = "Firestore နှင့် Storage တွင် ပြင်ဆင်နေပါသည်..."

            val imageList = if (property.imageResName.contains(",")) {
                property.imageResName.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                listOf(property.imageResName)
            }

            repository.updatePropertyWithFirebase(property, imageList)

            isLoading.value = false
            onSuccess()
        }
    }

    fun deleteProperty(
        propertyId: Long,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val currentUid = currentUserId()
        val admin = isAdmin()
        if (!isUserSignedIn() || currentUid.isBlank()) {
            onError?.invoke("Guest မဖြစ်နိုင်ပါ။ ကြော်ငြာ ဖျက်သိမ်းရန် အကောင့်ဝင်ရောက်ပါ")
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            loadingMessage.value = "Firebase မှ ဖျက်သိမ်းနေပါသည်..."

            repository.getPropertyById(propertyId).collect { property ->
                if (property != null) {
                    val isAgencyListing = property.agentType != "User Post" && property.agentType != "Direct Post"
                    val isOwner = (property.userId.isNotBlank() && property.userId == currentUid) ||
                            (property.userId.isBlank() && (property.agentType == "Direct Post" || property.agentType == "User Post"))
                    val canModify = if (isAgencyListing) admin else (isOwner || admin)

                    if (!canModify) {
                        isLoading.value = false
                        val errMsg = if (isAgencyListing) "အကျိုးဆောင် ကြော်ငြာများကို Admin တစ်ဦးတည်းသာ ဖျက်ပိုင်ခွင့် ရှိပါသည်"
                                     else "မိမိ ပိုင်ဆိုင်သော ကြော်ငြာများ သို့မဟုတ် Admin သာလျှင် ဖျက်ခွင့် ရှိပါသည်"
                        onError?.invoke(errMsg)
                        return@collect
                    }
                    repository.deletePropertyWithFirebase(property)
                    isLoading.value = false
                    onSuccess()
                } else {
                    isLoading.value = false
                }
            }
        }
    }

    fun resetFilters() {
        searchQuery.value = ""
        selectedListingTypeTab.value = "ALL"
        selectedCity.value = "ALL"
        selectedPropertyType.value = "ALL"
        maxPriceLakhs.value = 20000f
        minBedrooms.value = 0
    }
}
