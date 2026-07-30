package com.example.data.remote

import com.example.data.model.Property
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CloudPropertyDto(
    val cloudId: String? = null,
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
    val agentType: String = "User Post",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toProperty(localId: Long = 0, isFav: Boolean = false): Property {
        return Property(
            id = localId,
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
            isFavorite = isFav,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromProperty(p: Property, cloudKey: String? = null): CloudPropertyDto {
            return CloudPropertyDto(
                cloudId = cloudKey,
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
                createdAt = p.createdAt
            )
        }
    }
}

data class PostResponse(
    val name: String? = null // Firebase RTDB returns generated key in "name"
)

interface CloudPropertyApi {

    @GET("properties.json")
    suspend fun getAllCloudProperties(): Response<Map<String, CloudPropertyDto>?>

    @POST("properties.json")
    suspend fun postCloudProperty(@Body dto: CloudPropertyDto): Response<PostResponse>
}

object CloudNetworkClient {
    private const val BASE_URL = "https://grace-realestate-mm-default-rtdb.asia-southeast1.firebasedatabase.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: CloudPropertyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CloudPropertyApi::class.java)
    }
}
