package com.albertjk.dishrecommender

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Dish::class,
    parentColumns = ["dishId"],
    childColumns = ["dishId"],
    onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = DailyLog::class,
        parentColumns = ["logDate"],
        childColumns = ["logDate"],
        onDelete = ForeignKey.CASCADE)]
)
data class DishLog(

    // Member variables representing the table columns.
    @PrimaryKey(autoGenerate = true)
    val logId: Int,

    val dishId: Int,
    val mealName: String,

    val logDate: Date
)