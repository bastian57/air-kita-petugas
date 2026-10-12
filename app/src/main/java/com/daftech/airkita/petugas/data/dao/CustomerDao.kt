package com.daftech.airkita.petugas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daftech.airkita.petugas.data.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE")
    fun all(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE")
    suspend fun allOnce(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun byId(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Customer>)

    @Query("DELETE FROM customers")
    suspend fun clear()

    @Query("UPDATE customers SET lastMeter = :meter WHERE id = :id")
    suspend fun updateLastMeter(id: Long, meter: Int)
}
