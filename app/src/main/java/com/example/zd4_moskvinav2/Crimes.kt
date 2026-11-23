package com.example.zd4_moskvinav2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Crimes: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crimes)
        val h = CrimeDataBase.getInstance(this).crimeDao()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                h.clearCrimes()
                h.addCrime(Crime(0L, getString(R.string.crime_1), getString(R.string.date_1),false,"Michale","929485739285"))
                h.addCrime(Crime(1L, getString(R.string.crime_2), getString(R.string.date_2),false,"Killw","3457345234573"))
                h.addCrime(Crime(2L, getString(R.string.crime_3), getString(R.string.date_3),false,"Hello","4567423452"))
                h.addCrime(Crime(3L, getString(R.string.crime_4), getString(R.string.date_4),false,"World","2345756434234"))
                h.addCrime(Crime(4L, getString(R.string.crime_5), getString(R.string.date_5),false,"Matv","732532452345"))
            }

        }
        findViewById<LinearLayout>(R.id.crime1).setOnClickListener {
            openDetail(0L, getString(R.string.crime_1), getString(R.string.date_1))
        }
        findViewById<LinearLayout>(R.id.crime2).setOnClickListener {
            openDetail(1L, getString(R.string.crime_2), getString(R.string.date_2))
        }
        findViewById<LinearLayout>(R.id.crime3).setOnClickListener {
            openDetail(2L, getString(R.string.crime_3), getString(R.string.date_3))
        }
        findViewById<LinearLayout>(R.id.crime4).setOnClickListener {
            openDetail(3L, getString(R.string.crime_4), getString(R.string.date_4))
        }
        findViewById<LinearLayout>(R.id.crime5).setOnClickListener {
            openDetail(4L, getString(R.string.crime_5), getString(R.string.date_5))
        }
    }
    private fun openDetail(index: Long, title: String, date: String) {
        val pref = getSharedPreferences("crime", Context.MODE_PRIVATE)
        pref.edit().putLong(MainActivity.EXTRA_CRIME_ID, index)
            .putString("title", title)
            .putString("date", date).apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}