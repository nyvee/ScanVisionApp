package com.scanvision.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Insert
    fun insertResult(result: ResultEntity)

    @Query("SELECT * FROM results")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Delete
    fun deleteResult(result: ResultEntity)
}