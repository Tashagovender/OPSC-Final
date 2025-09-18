package com.example.versiononetasktwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.*

class Calender : AppCompatActivity() {
    lateinit var calendarView: CalendarView
    lateinit var edEventName: EditText
    lateinit var btnSaveEvent: Button
    private lateinit var database: DatabaseReference
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("Calender")

        // Typecast views
        calendarView = findViewById(R.id.calendarView)
        edEventName = findViewById(R.id.edEventName)
        btnSaveEvent = findViewById(R.id.btnSaveEvent)

        // Set up the CalendarView date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val monthString = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            selectedDate = "$year-$monthString-$dayString"

            // Fetch event for the selected date
            fetchEventForDate(selectedDate)
        }

        // Set up the button click listener
        btnSaveEvent.setOnClickListener {
            val eventName = edEventName.text.toString()
            if (eventName.isNotEmpty() && selectedDate.isNotEmpty()) {
                saveToFirebase(eventName, selectedDate)
            } else {
                Toast.makeText(this, "Please enter an event name and select a date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToFirebase(eventName: String, date: String) {
        val event = mapOf(
            "eventName" to eventName,
            "date" to date
        )

        val key = database.child(date).key
        if (key != null) {
            database.child(date).setValue(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event saved successfully", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { err ->
                    Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun fetchEventForDate(date: String) {
        database.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val eventName = snapshot.child("eventName").getValue(String::class.java)
                    edEventName.setText(eventName)
                } else {
                    edEventName.setText("")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Calender, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
