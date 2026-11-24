package com.example.zd4_moskvinav2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Crimes : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CrimeAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var btnAddCrime: Button
    private lateinit var crimeDao: CrimeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crimes)

        crimeDao = CrimeDataBase.getInstance(this).crimeDao()

        recyclerView = findViewById(R.id.recycler_view_crimes)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        btnAddCrime = findViewById(R.id.btn_add_crime)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CrimeAdapter { crime ->
            openDetail(crime.id, crime.title, crime.date)
        }
        recyclerView.adapter = adapter

        loadCrimes()

        btnAddCrime.setOnClickListener {
            showAddCrimeDialog()
        }
    }

    private fun loadCrimes() {
        lifecycleScope.launch {
            val crimes = withContext(Dispatchers.IO) {
                crimeDao.getAllCrimes()
            }

            if (crimes.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
                adapter.submitList(crimes)
            }
        }
    }

    private fun showAddCrimeDialog() {
        val inputTitle = EditText(this).apply { hint = "Название преступления" }
        val inputDate = EditText(this).apply {
            hint = "Дата (например, 25.11.2026)"
            setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()))
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(inputTitle)
            addView(inputDate)
        }

        AlertDialog.Builder(this)
            .setTitle("Новое преступление")
            .setView(layout)
            .setPositiveButton("Создать") { _, _ ->
                val title = inputTitle.text.toString().trim()
                val date = inputDate.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Введите название!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val newCrime = Crime(
                            id = System.currentTimeMillis(),
                            title = title,
                            date = date,
                            isSolved = false,
                            suspect = "",
                            suspectPhone = ""
                        )
                        crimeDao.addCrime(newCrime)
                    }
                    loadCrimes()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openDetail(id: Long, title: String, date: String) {
        val pref = getSharedPreferences("crime", MODE_PRIVATE)
        pref.edit()
            .putLong(MainActivity.EXTRA_CRIME_ID, id)
            .putString("title", title)
            .putString("date", date)
            .apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}