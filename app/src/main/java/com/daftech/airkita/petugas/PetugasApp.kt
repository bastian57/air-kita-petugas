package com.daftech.airkita.petugas

import android.app.Application
import androidx.room.Room
import com.daftech.airkita.petugas.data.AppDatabase
import com.daftech.airkita.petugas.data.Repository

class PetugasApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "petugas.db").build()
    }
    val repository: Repository by lazy { Repository(database) }
}
