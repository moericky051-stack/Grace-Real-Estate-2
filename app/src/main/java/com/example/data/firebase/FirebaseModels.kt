package com.example.data.firebase

import com.example.data.model.Property

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val agencyName: String = "",
    val photoUrl: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestorePropertyDto(
    val docId: String = "",
    val userId: String = "",
    val title: String = "",
    val listingType: String = "BUY",
    val propertyType: String = "Condo",
    val priceLakhs: Double = 0.0,
    val pricePeriod: String = "TOTAL",
    val city: String = "Yangon",
    val township: String = "",
    val address: String = "",
    val areaSqft: Int = 1000,
    val bedrooms: Int = 2,
    val bathrooms: Int = 1,
    val floorLevel: String = "3rd Floor",
    val furnishing: String = "Fully Furnished",
    val deedType: String = "Grant Land",
    val description: String = "",
    val imageResName: String = "img_hero_banner",
    val agentName: String = "Grace Real Estate",
    val agentPhone: String = "09450012345",
    val agentType: String = "Verified Agent",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toProperty(localId: Long = 0, isFav: Boolean = false): Property {
        return Property(
            id = localId,
            docId = docId,
            userId = userId,
            title = title,
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
            imageResName = imageResName,
            agentName = agentName,
            agentPhone = agentPhone,
            agentType = agentType,
            status = status,
            isFavorite = isFav,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromProperty(p: Property, idOverride: String? = null): FirestorePropertyDto {
            return FirestorePropertyDto(
                docId = idOverride ?: p.docId,
                userId = p.userId,
                title = p.title,
                listingType = p.listingType,
                propertyType = p.propertyType,
                priceLakhs = p.priceLakhs,
                pricePeriod = p.pricePeriod,
                city = p.city,
                township = p.township,
                address = p.address,
                areaSqft = p.areaSqft,
                bedrooms = p.bedrooms,
                bathrooms = p.bathrooms,
                floorLevel = p.floorLevel,
                furnishing = p.furnishing,
                deedType = p.deedType,
                description = p.description,
                imageResName = p.imageResName,
                agentName = p.agentName,
                agentPhone = p.agentPhone,
                agentType = p.agentType,
                status = p.status,
                createdAt = p.createdAt
            )
        }
    }
}
