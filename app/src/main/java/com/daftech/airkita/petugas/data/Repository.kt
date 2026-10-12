package com.daftech.airkita.petugas.data

import com.daftech.airkita.petugas.data.entity.Customer
import com.daftech.airkita.petugas.data.entity.Reading
import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    fun customers(): Flow<List<Customer>> = db.customerDao().all()

    suspend fun addCustomers(list: List<Customer>) = db.customerDao().insertAll(list)

    suspend fun clearCustomers() = db.customerDao().clear()

    suspend fun getCustomer(id: Long): Customer? = db.customerDao().byId(id)

    /** Simpan hasil pencatatan meter; sekaligus perbarui meter terakhir pelanggan. */
    suspend fun saveReading(customerId: Long, meterEnd: Int) {
        db.readingDao().insert(
            Reading(customerId = customerId, meterEnd = meterEnd, recordedAt = System.currentTimeMillis())
        )
        db.customerDao().updateLastMeter(customerId, meterEnd)
    }

    suspend fun readingsOnce(): List<Reading> = db.readingDao().allOnce()

    suspend fun readingFor(customerId: Long): Reading? = db.readingDao().byCustomer(customerId)

    suspend fun readingsCount(): Int = db.readingDao().count()

    suspend fun clearReadings() = db.readingDao().clear()
}
