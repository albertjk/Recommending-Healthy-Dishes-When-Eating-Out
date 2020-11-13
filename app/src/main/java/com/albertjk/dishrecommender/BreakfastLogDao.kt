package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BreakfastLogDao {

    @Insert
    suspend fun insertBreakfastLog(breakfastLog: BreakfastLog)

    @Update
    suspend fun updateBreakfastLog(breakfastLog: BreakfastLog)

    @Delete
    suspend fun deleteBreakfastLog(breakfastLog: BreakfastLog)

    @Query("SELECT * FROM BreakfastLog")
    fun getBreakfastLogs(): LiveData<List<BreakfastLog>>
}