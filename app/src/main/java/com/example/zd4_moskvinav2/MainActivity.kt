package com.example.zd4_moskvinav2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest

class MainActivity : AppCompatActivity() {
    private var crimeId: Long = -1
    private lateinit var crime: Crime
    private lateinit var crimeRepository: CrimeDao
    private lateinit var btnChooseSuspect: Button
    private lateinit var btnCallSuspect: Button
    private lateinit var btnDate: Button
    private lateinit var btnSendReport: Button
    private lateinit var ettitles: EditText
    private lateinit var CheckBoxs: CheckBox
    private lateinit var pref : SharedPreferences
    private lateinit var btnSaveCrime: Button
    private var pendingContactUri: Uri? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pendingContactUri?.let { uri ->
                processContactData(uri)
            }
        } else {
            Toast.makeText(this, "Без разрешения нельзя прочитать номер телефона", Toast.LENGTH_SHORT).show()
            pendingContactUri = null
        }
    }
    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val contactUri: Uri? = result.data?.data
            contactUri?.let { uri ->
                processContactSelection(uri)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pref = getSharedPreferences("crime",Context.MODE_PRIVATE)
        crimeId = pref.getLong(EXTRA_CRIME_ID, -1)
        CheckBoxs = findViewById(R.id.solvedds)
        btnSendReport = findViewById(R.id.crime_send_report)
        btnDate = findViewById(R.id.dates)
        ettitles = findViewById(R.id.titles)
        btnSaveCrime = findViewById(R.id.btn_save_crime)
        if (crimeId == -1L) {
            Toast.makeText(this, "Ошибка: преступление не выбрано", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val initialTitle = pref.getString("title","") ?: ""
        val initialDate =  pref.getString("date","") ?: ""
        ettitles.setText(initialTitle)
        crimeRepository = CrimeDataBase.getInstance(applicationContext).crimeDao()

        btnChooseSuspect = findViewById(R.id.crime_suspect)
        btnCallSuspect = findViewById(R.id.crime_call_suspect)

        loadCrime(initialTitle, initialDate)
        setupListeners()
        btnDate.setText(initialDate)
        CheckBoxs.setOnClickListener{
            if(CheckBoxs.isChecked) {
                Toast.makeText(this, getString(R.string.solved), Toast.LENGTH_SHORT).show()
                crime.isSolved = true;
            }
            else {
                Toast.makeText(this, getString(R.string.nosolved), Toast.LENGTH_SHORT).show()
                crime.isSolved = false;
            }
        }
        btnSendReport.setOnClickListener{
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,Gson().toJson(crime))
                putExtra(Intent.EXTRA_SUBJECT,getString(R.string.t)).also {
                        intent -> startActivity(intent)
                    val chooserIntent =
                        Intent.createChooser(intent,
                            getString(R.string.sendreport))
                    startActivity(chooserIntent)
                }
            }
        }
        btnSaveCrime.setOnClickListener { setupSaveButton() }
    }
    private fun loadCrime(initialTitle: String, initialDate: String) {
        lifecycleScope.launch {
            val loadedCrime = withContext(Dispatchers.IO) {
                crimeRepository.getCrime(crimeId)
            }

            if (loadedCrime != null) {
                crime = loadedCrime
            } else {
                crime = Crime(
                    id = crimeId,
                    title = initialTitle,
                    date = initialDate
                )
                withContext(Dispatchers.IO) {
                    crimeRepository.addCrime(crime)
                }
            }
            updateUI()
        }
    }

    private fun setupListeners() {
        btnChooseSuspect.setOnClickListener {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            pickContactLauncher.launch(pickContactIntent)
        }
        btnCallSuspect.setOnClickListener {
            if (crime.suspectPhone.isNotBlank()) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${crime.suspectPhone}"))
                startActivity(dialIntent)
            }
            else{
                Toast.makeText(this, "Нет номера", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun processContactSelection(contactUri: Uri) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            processContactData(contactUri)
        } else {
            pendingContactUri = contactUri
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    private fun processContactData(contactUri: Uri) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val queryFields = arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
                )

                contentResolver.query(contactUri, queryFields, null, null, null)?.use { cursor ->
                    if (cursor.count == 0) return@use
                    cursor.moveToFirst()

                    val suspectName = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                    val contactId = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                    )

                    crime.suspect = suspectName

                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { pCursor ->
                        if (pCursor.count > 0) {
                            pCursor.moveToFirst()
                            crime.suspectPhone = pCursor.getString(
                                pCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )?.replace(Regex("[\\s-]"), "") ?: ""
                        } else {
                            crime.suspectPhone = ""
                        }
                    }

                    crimeRepository.updateCrime(crime)
                }
            }
            updateUI()
        }
    }

    private fun updateUI() {
        if (!::crime.isInitialized) return

        btnChooseSuspect.text = if (crime.suspect.isNotBlank()) {
            crime.suspect
        } else {
            getString(R.string.t)
        }

        if (crime.suspectPhone.isNotBlank()) {
            btnCallSuspect.text = getString(R.string.t2)
            btnCallSuspect.isEnabled = true
        } else {
            btnCallSuspect.text = getString(R.string.t3)
            btnCallSuspect.isEnabled = false
        }
    }

    fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Выполнено")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
    companion object {
        const val EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id"
    }
    private fun setupSaveButton() {
        btnSaveCrime.setOnClickListener {
            crime.title = ettitles.text.toString()
            crime.date = btnDate.text.toString()
            crime.isSolved = CheckBoxs.isChecked

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    crimeRepository.updateCrime(crime)
                }
                Toast.makeText(this@MainActivity, "Сохранено!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}