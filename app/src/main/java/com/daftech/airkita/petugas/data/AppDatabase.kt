package com.daftech.airkita.petugas.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.daftech.airkita.petugas.data.dao.CustomerDao
import com.daftech.airkita.petugas.data.dao.ReadingDao
import com.daftech.airkita.petugas.data.entity.Customer
import com.daftech.airkita.petugas.data.entity.Reading

@Database(entities = [Customer::class, Reading::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun readingDao(): ReadingDao
}
