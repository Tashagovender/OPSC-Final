package com.example.versiononetasktwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Monthly : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly)

        barChart = findViewById(R.id.barChart)
        database = FirebaseDatabase.getInstance().reference

        fetchMonthlyData()
    }

    private fun fetchMonthlyData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val oneMonthAgo = dateFormat.format(calendar.time)

        database.child("items")
            .orderByChild("startDateString")
            .startAt(oneMonthAgo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val barEntries = ArrayList<BarEntry>()
                        val labels = ArrayList<String>()
                        var index = 0

                        for (taskSnapshot in snapshot.children) {
                            val task = taskSnapshot.getValue(TaskModel::class.java)
                            task?.let {
                                val totalTime = it.totalTimeString?.split(":")
                                val hours = totalTime?.get(0)?.toFloatOrNull() ?: 0f
                                val minutes = totalTime?.get(1)?.toFloatOrNull() ?: 0f
                                val totalHours = hours + minutes / 60
                                val mingoal = it.minGoalString ?: "N/A"
                                val maxgoal = it.maxGoalString ?: "N/A"

                                barEntries.add(BarEntry(index.toFloat(), totalHours))
                                labels.add("${it.startDateString ?: ""}\n(Min-$mingoal, Max-$maxgoal)")
                                index++
                            }
                        }

                        val barDataSet = BarDataSet(barEntries, "Hours Worked")
                        val barData = BarData(barDataSet)
                        barChart.data = barData

                        val xAxis = barChart.xAxis
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        xAxis.granularity = 1f
                        xAxis.setDrawGridLines(false)

                        barChart.axisLeft.axisMinimum = 0f
                        barChart.axisRight.isEnabled = false
                        barChart.description.isEnabled = false
                        barChart.invalidate()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.e("MonthlyProgressActivity", "Database error: ${error.message}")
                    Toast.makeText(this@Monthly, "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}