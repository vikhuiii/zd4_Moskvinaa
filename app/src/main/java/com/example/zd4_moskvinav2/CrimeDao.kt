package com.example.zd4_moskvinav2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CrimeDao {
    @Query("SELECT * FROM Crime")
    suspend fun getAllCrimes(): List<Crime>

    @Query("SELECT * FROM crime WHERE id = :id")
    fun getCrime(id: Long): Crime

    @Insert
    fun addCrime(crime: Crime)

    @Update
    fun updateCrime(crime: Crime)

    @Query("DELETE FROM crime")
    fun clearCrimes()
}