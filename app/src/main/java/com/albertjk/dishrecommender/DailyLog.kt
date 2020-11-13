package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class DailyLog(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = false)
    val logDate: Date,

    val dailyCaloriesIntake: Int,
    val dailyTotalFatIntake: Float,
    val dailySaturatedFatIntake: Float,
    val dailyCarbohydratesIntake: Float,
    val dailySugarsIntake: Float,
    val dailySaltIntake: Float,
    val dailyFibreIntake: Float,
    val dailyProteinIntake: Float
) {
    /**
     * Returns the property value associated with the string property name.
     * This function is needed to dynamically get the nutrition data when displaying them
     * for the pie chart in DailyNutritionPieChartFragment.kt.
     */
    fun getProperty(propertyName: String): Float {
        return when (propertyName) {
            "Calories" -> {
                this.dailyCaloriesIntake.toFloat()
            }
            "Fat" -> {
                this.dailyTotalFatIntake
            }
            "Sat. fat" -> {
                this.dailySaturatedFatIntake
            }
            "Carbs" -> {
                this.dailyCarbohydratesIntake
            }
            "Sugars" -> {
                this.dailySugarsIntake
            }
            "Salt" -> {
                this.dailySaltIntake
            }
            "Fibre" -> {
                this.dailyFibreIntake
            }
            else -> {
                this.dailyProteinIntake
            }
        }
    }
}