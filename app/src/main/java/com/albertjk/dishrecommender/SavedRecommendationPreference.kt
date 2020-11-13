package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SavedRecommendationPreference (

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = true)
    val preferenceId: Int,

    val preferenceName: String,
    val selectedPriority: Int

)