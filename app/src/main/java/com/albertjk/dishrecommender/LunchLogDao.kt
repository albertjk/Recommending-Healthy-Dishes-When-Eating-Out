package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LunchLogDao {

    @Insert
    suspend fun insertLunchLog(lunchLog: LunchLog)

    @Update
    suspend fun updateLunchLog(lunchLog: LunchLog)

    @Delete
    suspend fun deleteLunchLog(lunchLog: LunchLog)

    @Query("SELECT * FROM LunchLog")
    fun getLunchLogs(): LiveData<List<LunchLog>>
}