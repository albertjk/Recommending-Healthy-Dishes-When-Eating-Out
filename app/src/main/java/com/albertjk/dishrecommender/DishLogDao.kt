package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DishLogDao {

    @Insert
    suspend fun insertDishLog(dishLog: DishLog)

    @Query("SELECT * FROM DishLog")
    fun getDishLogs(): LiveData<List<DishLog>>

    @Delete
    suspend fun deleteDishLog(dishLog: DishLog)
}
