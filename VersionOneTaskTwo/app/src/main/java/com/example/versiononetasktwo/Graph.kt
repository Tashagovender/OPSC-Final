package com.example.versiononetasktwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Graph : AppCompatActivity() {
    lateinit var lineChart: LineChart
    lateinit var database: DatabaseReference
    lateinit var datePicker: DatePicker
    lateinit var DataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        lineChart = findViewById(R.id.lineChart)
        datePicker = findViewById(R.id.datePicker)
        DataButton = findViewById(R.id.btnData)
        database = FirebaseDatabase.getInstance().reference

        DataButton.setOnClickListener {
            fetchAndDisplayGraph()
        }
    }

    private fun fetchAndDisplayGraph() {
        val selectedDate = "${datePicker.year}-${
            (datePicker.month + 1).toString().padStart(2, '0')
        }-${datePicker.dayOfMonth.toString().padStart(2, '0')}"
        database.child("items").orderByChild("startDateString").equalTo(selectedDate)
            .get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val entries = ArrayList<Entry>()
                    val minGoalEntries = ArrayList<Entry>()
                    val maxGoalEntries = ArrayList<Entry>()

                    // Add initial point (0,0)
                    entries.add(Entry(0f, 0f))
                    minGoalEntries.add(Entry(0f, 0f))
                    maxGoalEntries.add(Entry(0f, 0f))

                    var index = 1f
                    dataSnapshot.children.forEach { snapshot ->
                        val task = snapshot.getValue(TaskModel::class.java)
                        task?.let {
                            val totalHours =
                                it.totalTimeString?.split(":")?.let { timeParts ->
                                    val hours = timeParts[0].toFloat()
                                    val minutes = timeParts[1].toFloat()
                                    hours + (minutes / 60)
                                } ?: 0f

                            val minGoal = it.minGoalString?.toFloat() ?: 0f
                            val maxGoal = it.maxGoalString?.toFloat() ?: 0f

                            entries.add(Entry(index, totalHours))
                            minGoalEntries.add(Entry(index, minGoal))
                            maxGoalEntries.add(Entry(index, maxGoal))

                            index++
                        }
                    }

                    val lineDataSet = LineDataSet(entries, "Total Hours Worked")
                    lineDataSet.color = resources.getColor(R.color.teal_200)
                    lineDataSet.valueTextColor = resources.getColor(R.color.black)
                    lineDataSet.setDrawCircles(false)
                    lineDataSet.lineWidth = 3f
                    lineDataSet.mode = LineDataSet.Mode.LINEAR

                    val minGoalDataSet = LineDataSet(minGoalEntries, "Min Goal")
                    minGoalDataSet.color = resources.getColor(R.color.purple_200)
                    minGoalDataSet.valueTextColor = resources.getColor(R.color.black)
                    minGoalDataSet.setDrawCircles(false)
                    minGoalDataSet.enableDashedLine(10f, 5f, 0f)
                    minGoalDataSet.lineWidth = 3f
                    minGoalDataSet.mode = LineDataSet.Mode.LINEAR

                    val maxGoalDataSet = LineDataSet(maxGoalEntries, "Max Goal")
                    maxGoalDataSet.color = resources.getColor(R.color.purple_500)
                    maxGoalDataSet.valueTextColor = resources.getColor(R.color.black)
                    maxGoalDataSet.setDrawCircles(false)
                    maxGoalDataSet.enableDashedLine(10f, 5f, 0f)
                    maxGoalDataSet.lineWidth = 3f
                    maxGoalDataSet.mode = LineDataSet.Mode.LINEAR

                    val lineData = LineData(lineDataSet, minGoalDataSet, maxGoalDataSet)
                    lineChart.data = lineData

                    lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    lineChart.axisLeft.setDrawGridLines(false)
                    lineChart.axisRight.setDrawGridLines(false)
                    lineChart.axisRight.isEnabled = false
                    lineChart.description.isEnabled = false
                    lineChart.invalidate()

                } else {
                    Toast.makeText(
                        this,
                        "No records found for the selected date",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }
}

