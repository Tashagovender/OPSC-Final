package com.example.versiononetasktwo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import java.sql.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class MainActivity : AppCompatActivity() {
//variables
    lateinit var spinner: Spinner
    lateinit var editName: EditText
    lateinit var editDescription: EditText
    lateinit var minGoal:EditText
    lateinit var maxGoal:EditText
    lateinit var btnstart_date: Button
    lateinit var btnstart_time: Button
    lateinit var btnend_date: Button
    lateinit var btnend_time: Button
    lateinit var btnSave: Button
    //lateinit var btnNavCamera: Button//navigates to camera
    lateinit var btnAdvCamera: Button
    lateinit var btnReadAll: Button
    lateinit var btnGraphFeatures:Button
    lateinit var btnCalender: Button
    lateinit var btnProgress: Button
    lateinit var btnCalenderList:Button


    //globals--checks none are nulls
    var start_date: Date? = null
    var start_time: Date? = null
    var end_date: Date? = null
    var end_time: Date? = null

    //Firebase ref
    lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        //typecast
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spinner = findViewById(R.id.spinner)
        editName = findViewById(R.id.editName)
        editDescription = findViewById(R.id.editDescription)
        minGoal=findViewById(R.id.minGoal)
        maxGoal=findViewById(R.id.maxGoal)
        btnstart_date = findViewById(R.id.btnstart_date)
        btnstart_time = findViewById(R.id.btnstart_time)
        btnend_date = findViewById(R.id.btnend_date)
        btnend_time = findViewById(R.id.btnend_time)
        btnSave = findViewById(R.id.btnSave)
      //  btnNavCamera=findViewById(R.id.btnCamActivity)
        btnAdvCamera=findViewById(R.id.btnCameraTwo)
        btnReadAll=findViewById(R.id.btnRead)
        btnGraphFeatures=findViewById(R.id.GraphFeatures)
        btnCalender=findViewById(R.id.btnCalender)
        btnProgress  =findViewById(R.id.btnProgress)
        btnCalenderList=findViewById(R.id.btnCalenderList)

        //database object
        database = FirebaseDatabase.getInstance().reference
        //set spinner adapter
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_dropdown_item
        )
        spinner.adapter = spinnerAdapter
        //test click events
        btnstart_date.setOnClickListener { showDatePicker(startDateListener) }
        btnstart_time.setOnClickListener { showTimePicker(startTimeListener) }
        btnend_date.setOnClickListener { showDatePicker(endDateListener) }
        btnend_time.setOnClickListener { showTimePicker(endtimeListener) }
        btnSave.setOnClickListener {

            val selectedItem = spinner.selectedItem as String

            val taskName = editName.text.toString()

            val taskDesc = editDescription.text.toString()

            if (taskName.isEmpty()) {

                editName.error = "Please enter name"

                return@setOnClickListener

            }

            if (taskDesc.isEmpty()) {

                editDescription.error = "Please enter age"

                return@setOnClickListener

            }

            saveToFirebase(selectedItem, taskName, taskDesc)

        }//firebase ends
        //takes to specific screen
      //  btnNavCamera.setOnClickListener{
          //  val intent= Intent (this,Camera::class.java)
           // startActivity(intent)

        btnAdvCamera.setOnClickListener{
            val intentTwo= Intent (this,CameraTwo::class.java)
            startActivity(intentTwo)
        }
        btnReadAll.setOnClickListener{
            fetchAndDisplay()
        }
        btnGraphFeatures.setOnClickListener {
            val intentThree = Intent(this, Graph::class.java)
            startActivity(intentThree)
        }
        btnCalender.setOnClickListener{
            val intentFour= Intent(this,Calender::class.java)
            startActivity(intentFour)
        }
        btnProgress.setOnClickListener{
            val intentFive=Intent(this,Monthly::class.java)
            startActivity(intentFive)
        }
        btnCalenderList.setOnClickListener{
            val intentSix=Intent(this,Event::class.java)
            startActivity(intentSix)
        }



    }

    //handling
    fun showDatePicker(dateSetListener: DatePickerDialog.OnDateSetListener) {
        //calender--> year/month/date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener, year, month, day
        )
        datePickerDialog.show()
    }


    //time method
    fun showTimePicker(timeSetListener: TimePickerDialog.OnTimeSetListener) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this,
            timeSetListener, hour, minute, true
        )
        timePickerDialog.show()
    }

    //variables and formatting for date time-->db
    val startDateListener =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day)
            start_date = selectedCalendar.time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDateString = dateFormat.format(start_date!!)
            btnstart_date.text = selectedDateString

        }


    // time listner
    private val startTimeListener =
        TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->

            //HH:mm
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = start_date
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedCalendar.set(Calendar.MINUTE, minute)
            start_time = selectedCalendar.time
            //format
            val timeformat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedTimeString = timeformat.format(start_time)
            btnstart_time.text = selectedTimeString


        }

    //end date listner
    private val endDateListener =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day)
            end_date = selectedCalendar.time

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDateString = dateFormat.format(end_date)
            btnend_date.text = selectedDateString


        }

    //end time listener
    private val endtimeListener =
        TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = end_date
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedCalendar.set(Calendar.MINUTE, minute)
            end_time = selectedCalendar.time

            //format
            val timeformat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedTimeString = timeformat.format(end_time!!)
            btnend_time.text = selectedTimeString

        }

    //firebase method
    private fun saveToFirebase(item: String, taskName: String, taskDesc: String) {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val minGoalString=minGoal.text.toString()

        val maxGoalString=maxGoal.text.toString()

        val startDateString = btnstart_date.text.toString()

        val startTimeString = btnstart_time.text.toString()

        val endDateString = btnend_date.text.toString()

        val endTimeString = btnend_time.text.toString()



        val startDate = dateFormat.parse(startDateString)

        val startTime = timeFormat.parse(startTimeString)

        val endDate = dateFormat.parse(endDateString)

        val endTime = timeFormat.parse(endTimeString)


        val totalTimeInMillis = endDate.time - startDate.time + endTime.time -

                startTime.time

        val totalMinutes = totalTimeInMillis / (1000 * 60)

        val totalHours = totalMinutes / 60

        val minutesRemaining = totalMinutes % 60

        val totalTimeString = String.format(
            Locale.getDefault(),

            "%02d:%02d",

            totalHours, minutesRemaining
        )

        val key = database.child("items").push().key

        if (key != null) {

            val task = TaskModel(
                taskName, taskDesc,item,minGoalString,maxGoalString,startDateString, startTimeString, endDateString, endTimeString,
                totalTimeString
            )

            database.child("items").child(key).setValue(task)

                .addOnSuccessListener {


                    Toast.makeText(
                        this, "Data inserted successfully",

                        Toast.LENGTH_LONG
                    ).show()

                }

                .addOnFailureListener { err ->


                    Toast.makeText(
                        this, "Error: ${err.message}",

                        Toast.LENGTH_LONG
                    ).show()

                }

        }

    }
    fun fetchAndDisplay()
    {
        database.child("items").get().addOnSuccessListener{dataSnapshot->
            if(dataSnapshot.exists())
            {
                val records=ArrayList<String>()
                dataSnapshot.children.forEach{snapshot->
                    val task=snapshot.getValue(TaskModel::class.java)
                    task?.let{
                        records.add("Name: ${it.taskName}\n"+
                       "Description: ${it.taskDesc}\n"+
                        "Category: ${it.category}\n"+
                                "Min Hour Goal:${it.minGoalString}\n"+
                                "Max Hour Goal:${it.maxGoalString}\n"+
                        "Start Date: ${it.startDateString}\n"+
                        "Start time: ${it.startTimeString}\n"+
                        "End date: ${it.endDateString}\n"+
                        "End Time: ${it.endTimeString}\n")
                    }
                }
                displayRecordDialog(records)
            }else
            {
                Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
        }
    }//ends method
    //pass the data into a builder-> alertdb
    fun displayRecordDialog(records: ArrayList<String>)
    {
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Timesheet Entry:")
        val arrayAdapter=ArrayAdapter<String> (this,
        android.R.layout.simple_list_item_1,records)
        builder.setAdapter(arrayAdapter,null)
        builder.setPositiveButton("OK",null)
        builder.show()
    }
}

//data class
data class TaskModel (

    var taskName: String? = null,

    var taskDesc: String? =null,
    var category: String?=null,

    var minGoalString:String?=null,

    var maxGoalString:String?=null,

    var startDateString:String? =null,

    var startTimeString:String? =null,

    var endDateString:String? =null,

    var endTimeString:String? =null,

    var totalTimeString: String? = null,








)





















