package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class BreakfastLog(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = false)
    val logDate: Date,

    val breakfastCaloriesIntake: Int,
    val breakfastTotalFatIntake: Float,
    val breakfastSaturatedFatIntake: Float,
    val breakfastCarbohydratesIntake: Float,
    val breakfastSugarsIntake: Float,
    val breakfastSaltIntake: Float,
    val breakfastFibreIntake: Float,
    val breakfastProteinIntake: Float
) {
    /**
     * Returns the property value associated with the string property name.
     * This function is needed to dynamically get the nutrition data when displaying them
     * for the pie chart in DailyNutritionPieChartFragment.kt.
     */
    fun getProperty(propertyName: String): Float {
        return when (propertyName) {
            "Calories" -> {
                this.breakfastCaloriesIntake.toFloat()
            }
            "Fat" -> {
                this.breakfastTotalFatIntake
            }
            "Sat. fat" -> {
                this.breakfastSaturatedFatIntake
            }
            "Carbs" -> {
                this.breakfastCarbohydratesIntake
            }
            "Sugars" -> {
                this.breakfastSugarsIntake
            }
            "Salt" -> {
                this.breakfastSaltIntake
            }
            "Fibre" -> {
                this.breakfastFibreIntake
            }
            else -> {
                this.breakfastProteinIntake
            }
        }
    }
}