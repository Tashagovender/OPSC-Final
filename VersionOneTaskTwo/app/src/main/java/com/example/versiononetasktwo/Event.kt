package com.example.versiononetasktwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Event : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var tvEventDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("Calender")

        // Typecast views
        tvEventDetails = findViewById(R.id.tvEventDetails)
        val btnShowEvent: Button = findViewById(R.id.btnShowEvent)

        // Set up the button click listener
        btnShowEvent.setOnClickListener { fetchEvents() }
    }

    private fun fetchEvents() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val eventDetails = StringBuilder()
                for (snapshot in dataSnapshot.children) {
                    val date = snapshot.child("date").getValue(String::class.java)
                    val eventName = snapshot.child("eventName").getValue(String::class.java)
                    if (date != null && eventName != null) {
                        val daysLeft = calculateDaysLeft(date)
                        eventDetails.append("Event: ").append(eventName)
                            .append("\nDate: ").append(date)
                            .append("\nDays left: ").append(daysLeft).append("\n\n")
                    }
                }
                tvEventDetails.text = eventDetails.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Event, "Failed to fetch data: ${databaseError.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun calculateDaysLeft(eventDate: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = sdf.parse(eventDate)
            val today = Date()
            val diffInMillies = date.time - today.time
            TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        } catch (e: ParseException) {
            e.printStackTrace()
            -1
        }
    }
}