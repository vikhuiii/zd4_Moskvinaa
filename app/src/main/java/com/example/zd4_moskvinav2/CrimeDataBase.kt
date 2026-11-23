package com.example.zd4_moskvinav2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Crime::class], version = 2, exportSchema = false)
@TypeConverters(CrymeTypeConverts::class)
abstract class CrimeDataBase: RoomDatabase() {
    abstract fun crimeDao(): CrimeDao

    companion object {
        @Volatile private var obj: com.example.zd4_moskvinav2.CrimeDataBase? = null

        fun getInstance(context: Context): com.example.zd4_moskvinav2.CrimeDataBase =
            obj ?: synchronized(this) {
                obj ?: Room.databaseBuilder(
                    context.applicationContext,
                    com.example.zd4_moskvinav2.CrimeDataBase::class.java,
                    "my_hello2.db"
                ).build().also { obj = it }
            }
    }
}