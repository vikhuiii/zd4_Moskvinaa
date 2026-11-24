package com.example.zd4_moskvinav2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
                val crimes = withContext(Dispatchers.IO) {
                    h.getAllCrimes()
                }

                if (crimes.isEmpty()) {
                    findViewById<LinearLayout>(R.id.crime_list_container).visibility = View.GONE
                    findViewById<TextView>(R.id.empty_message).visibility = View.VISIBLE
                    findViewById<Button>(R.id.btn_add_crime).visibility = View.VISIBLE
                } else {
                    findViewById<LinearLayout>(R.id.crime_list_container).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.empty_message).visibility = View.GONE
                    findViewById<Button>(R.id.btn_add_crime).visibility = View.GONE

                    findViewById<LinearLayout>(R.id.crime1).setOnClickListener {
                        openDetail(crimes[0].id, crimes[0].title, crimes[0].date)
                    }
                    findViewById<LinearLayout>(R.id.crime2).setOnClickListener {
                        openDetail(crimes[0].id, crimes[0].title, crimes[0].date)
                    }
                    findViewById<LinearLayout>(R.id.crime3).setOnClickListener {
                        openDetail(crimes[0].id, crimes[0].title, crimes[0].date)
                    }
                    findViewById<LinearLayout>(R.id.crime4).setOnClickListener {
                        openDetail(crimes[0].id, crimes[0].title, crimes[0].date)
                    }
                    findViewById<LinearLayout>(R.id.crime5).setOnClickListener {
                        openDetail(crimes[0].id, crimes[0].title, crimes[0].date)
                    }

                }
            }

            findViewById<Button>(R.id.btn_add_crime).setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val newCrime = Crime(
                            id = System.currentTimeMillis(),
                            title = "Новое преступление",
                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            isSolved = false,
                            suspect = "",
                            suspectPhone = ""
                        )
                        h.addCrime(newCrime)
                    }
                    recreate()
                }
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