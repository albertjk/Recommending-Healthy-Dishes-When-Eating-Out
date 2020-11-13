package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DinnerLogDao {

    @Insert
    suspend fun insertDinnerLog(dinnerLog: DinnerLog)

    @Update
    suspend fun updateDinnerLog(dinnerLog: DinnerLog)

    @Delete
    suspend fun deleteDinnerLog(dinnerLog: DinnerLog)

    @Query("SELECT * FROM DinnerLog")
    fun getDinnerLogs(): LiveData<List<DinnerLog>>
}