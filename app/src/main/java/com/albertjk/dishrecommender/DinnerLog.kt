package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class DinnerLog(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = false)
    val logDate: Date,

    val dinnerCaloriesIntake: Int,
    val dinnerTotalFatIntake: Float,
    val dinnerSaturatedFatIntake: Float,
    val dinnerCarbohydratesIntake: Float,
    val dinnerSugarsIntake: Float,
    val dinnerSaltIntake: Float,
    val dinnerFibreIntake: Float,
    val dinnerProteinIntake: Float
) {
    /**
     * Returns the property value associated with the string property name.
     * This function is needed to dynamically get the nutrition data when displaying them
     * for the pie chart in DailyNutritionPieChartFragment.kt.
     */
    fun getProperty(propertyName: String): Float {
        return when (propertyName) {
            "Calories" -> {
                this.dinnerCaloriesIntake.toFloat()
            }
            "Fat" -> {
                this.dinnerTotalFatIntake
            }
            "Sat. fat" -> {
                this.dinnerSaturatedFatIntake
            }
            "Carbs" -> {
                this.dinnerCarbohydratesIntake
            }
            "Sugars" -> {
                this.dinnerSugarsIntake
            }
            "Salt" -> {
                this.dinnerSaltIntake
            }
            "Fibre" -> {
                this.dinnerFibreIntake
            }
            else -> {
                this.dinnerProteinIntake
            }
        }
    }
}