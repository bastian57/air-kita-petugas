package com.daftech.airkita.petugas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.daftech.airkita.petugas.data.entity.Reading

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings ORDER BY recordedAt DESC")
    suspend fun allOnce(): List<Reading>

    @Query("SELECT * FROM readings WHERE customerId = :customerId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun byCustomer(customerId: Long): Reading?

    @Insert
    suspend fun insert(r: Reading): Long

    @Query("DELETE FROM readings")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM readings")
    suspend fun count(): Int
}
