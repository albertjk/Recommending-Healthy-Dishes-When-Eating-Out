package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DailyLogDao {

    @Insert
    suspend fun insertDailyLog(dailyLog: DailyLog)

    @Update
    suspend fun updateDailyLog(dailyLog: DailyLog)

    @Delete
    suspend fun deleteDailyLog(dailyLog: DailyLog)

    @Query("SELECT * FROM DailyLog")
    fun getDailyLogs(): LiveData<List<DailyLog>>
}