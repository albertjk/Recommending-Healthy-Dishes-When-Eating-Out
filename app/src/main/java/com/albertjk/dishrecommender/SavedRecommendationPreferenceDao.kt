package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedRecommendationPreferenceDao {

    @Insert
    suspend fun insertAllRecommendationPreferences(savedRecommendationPreferences: List<SavedRecommendationPreference>)

    @Query("SELECT * FROM SavedRecommendationPreference")
    fun getRecommendationPreferences(): LiveData<List<SavedRecommendationPreference>>

    @Query("DELETE FROM SavedRecommendationPreference")
    suspend fun deleteAllRecommendationPreferences()
}