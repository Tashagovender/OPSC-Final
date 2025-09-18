package com.example.versiononetasktwo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class activity_Login : AppCompatActivity() {
    lateinit var btnLog: Button
    lateinit var edEmail: EditText
    lateinit var edPassword: EditText
    //firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //typecasts
        btnLog = findViewById(R.id.button)
        edEmail = findViewById(R.id.editTextText)
        edPassword = findViewById(R.id.editTextText2)

        //initialize firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        //btn click
        btnLog.setOnClickListener {
            val email = edEmail.text.toString().trim()
            val password = edPassword.text.toString().trim()

            //error checks
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter valid details",
                    Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }//on create ends

    //method
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "You are now logged in",
                        Toast.LENGTH_SHORT).show()
                    // Navigate to MainActivity only if login is successful
                    val intent = Intent(this@activity_Login,
                        MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Login Failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
