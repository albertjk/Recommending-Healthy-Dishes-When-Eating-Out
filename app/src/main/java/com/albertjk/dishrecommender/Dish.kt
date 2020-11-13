package com.albertjk.dishrecommender

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Dish @JvmOverloads constructor(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = true)
    val dishId: Int,

    val dishName: String,
    val calories: Int,
    val totalFat: Float,
    val saturatedFat: Float,
    val carbohydrates: Float,
    val sugars: Float,
    val salt: Float,
    val fibre: Float,
    val protein: Float,
    var isFavourite: Int,

    /* This field is not persisted to Room, so it is not stored in the database.
    This variable is needed to indicate if a recycler view row in the Favourite Dishes screen is
    expanded or not. By default, a row is not expanded. */
    @Ignore
    var isItemExpanded: Boolean = false,

    /* This field is not persisted to Room, so it is not stored in the database.
    It is only needed to temporarily store the dish's calculated healthiness score if the generally
    healthy recommendation preference was selected.
    @JvmOverloads is needed before the constructor keyword above to make Room ignore this field. */
    @Ignore
    var healthinessScore: Double? = null
) : Parcelable {

    /**
     * Returns the property value associated with the string property name.
     * This function is needed to dynamically sort the dish list by the appropriate property
     * in GeneratingRecommendationsFragment.kt.
     */
    fun getProperty(propertyName: String): Double {
        return when (propertyName) {
            "healthinessScore" -> {
                this.healthinessScore!!
            }
            "calories" -> {
                this.calories.toDouble()
            }
            "carbohydrates" -> {
                this.carbohydrates.toDouble()
            }
            else -> {
                this.protein.toDouble()
            }
        }
    }
}