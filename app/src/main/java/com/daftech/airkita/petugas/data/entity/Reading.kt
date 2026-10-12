package com.daftech.airkita.petugas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val meterEnd: Int,
    val recordedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
