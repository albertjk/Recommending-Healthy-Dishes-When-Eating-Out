package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LunchLog(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = false)
    val logDate: Date,

    val lunchCaloriesIntake: Int,
    val lunchTotalFatIntake: Float,
    val lunchSaturatedFatIntake: Float,
    val lunchCarbohydratesIntake: Float,
    val lunchSugarsIntake: Float,
    val lunchSaltIntake: Float,
    val lunchFibreIntake: Float,
    val lunchProteinIntake: Float
) {
    /**
     * Returns the property value associated with the string property name.
     * This function is needed to dynamically get the nutrition data when displaying them
     * for the pie chart in DailyNutritionPieChartFragment.kt.
     */
    fun getProperty(propertyName: String): Float {
        return when (propertyName) {
            "Calories" -> {
                this.lunchCaloriesIntake.toFloat()
            }
            "Fat" -> {
                this.lunchTotalFatIntake
            }
            "Sat. fat" -> {
                this.lunchSaturatedFatIntake
            }
            "Carbs" -> {
                this.lunchCarbohydratesIntake
            }
            "Sugars" -> {
                this.lunchSugarsIntake
            }
            "Salt" -> {
                this.lunchSaltIntake
            }
            "Fibre" -> {
                this.lunchFibreIntake
            }
            else -> {
                this.lunchProteinIntake
            }
        }
    }
}