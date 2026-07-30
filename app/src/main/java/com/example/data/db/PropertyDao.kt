package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Property
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {

    @Query("SELECT * FROM properties ORDER BY id DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<Property?>

    @Query("SELECT * FROM properties WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties ORDER BY id DESC")
    suspend fun getAllPropertiesList(): List<Property>

    @Query("SELECT COUNT(*) FROM properties WHERE title = :title AND agentPhone = :phone")
    suspend fun countByTitleAndPhone(title: String, phone: String): Int

    @Query("SELECT * FROM properties WHERE title = :title AND agentPhone = :phone LIMIT 1")
    suspend fun getPropertyByTitleAndPhone(title: String, phone: String): Property?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<Property>)

    @Query("UPDATE properties SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    @Query("SELECT * FROM properties WHERE docId = :docId LIMIT 1")
    suspend fun getPropertyByDocId(docId: String): Property?

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getPropertyCount(): Int
}
