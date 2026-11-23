package com.example.zd4_moskvinav2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crime")
data class Crime(
    @PrimaryKey
    var id: Long = 0,
    var title: String = "",
    var date: String = "1.1.1999",
    var isSolved: Boolean = false,
    var suspect: String = "",
    var suspectPhone: String = ""
)
