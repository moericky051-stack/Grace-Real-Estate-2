package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docId: String = "",
    val userId: String = "",
    val title: String, // ခေါင်းစဉ်
    val listingType: String, // "BUY" (ဝယ်ရန်/ရောင်းရန်) or "RENT" (ငှားရန်)
    val propertyType: String, // "Condo", "Apartment", "Land", "House", "Commercial"
    val priceLakhs: Double, // စျေးနှုန်း (သိန်းကျပ်)
    val pricePeriod: String = "TOTAL", // "TOTAL" or "PER_MONTH"
    val city: String, // ရန်ကုန်, မန္တလေး, နေပြည်တော်, တောင်ကြီး, ပြင်ဦးလွင်
    val township: String, // ကမာရွတ်, ဗဟန်း, စသည်
    val address: String, // အသေးစိတ် လိပ်စာ
    val areaSqft: Int, // စတုရန်းပေ
    val bedrooms: Int, // အိပ်ခန်း
    val bathrooms: Int, // ရေချိုးခန်း
    val floorLevel: String = "3rd Floor", // အလွှာ / အထပ်
    val furnishing: String = "Fully Furnished", // ပရိဘောဂ
    val deedType: String = "Grant Land (ဂရန်မြေ)", // စာရွက်စာတမ်း အထောက်အထား
    val description: String, // အသေးစိတ် ဖော်ပြချက်
    val imageResName: String = "img_hero_banner", // image drawable name or firebase storage URLs
    val agentName: String = "ဦးမင်းသူ (ရွှေအိမ် အကျိုးဆောင်)",
    val agentPhone: String = "09450012345",
    val agentType: String = "Verified Agent",
    val status: String = "ACTIVE", // "ACTIVE", "SOLD", "RENTED"
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
