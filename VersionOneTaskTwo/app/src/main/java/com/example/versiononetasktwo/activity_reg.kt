package com.example.versiononetasktwo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class activity_reg : AppCompatActivity() {
    //Variables
    private lateinit var edRegEmail: EditText
    private lateinit var edRgPassword: EditText
    private lateinit var edRegConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnNavigationLogin: Button
    //firebase
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg);
        //typeCast
        edRegEmail = findViewById(R.id.edRegEmail)
        edRgPassword = findViewById(R.id.edRegPassword)
        edRegConfirmPassword = findViewById(R.id.edConfirmPassword)
        btnRegister = findViewById(R.id.button2)
        btnNavigationLogin = findViewById(R.id.button3)
          //firebase
        mAuth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener()
        {
            registerUser()
        }

        btnNavigationLogin.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this@activity_reg, activity_Login::class.java)
            startActivity(intent)
        }



    }//on create ends

    //method to register
    private fun registerUser() {
        val email = edRegEmail.text.toString().trim()
        val password = edRgPassword.text.toString().trim()
        val confirmPass = edRegConfirmPassword.text.toString().trim()

        //validation checks
        try {
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Password cant be blank", Toast.LENGTH_SHORT).show()
                return
            }
            if (TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "Enter matching password", Toast.LENGTH_SHORT).show()
                return
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener()
                { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reg complete you must now login ", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Registration is complete", Toast.LENGTH_SHORT).show()
                    }
                }
            //highlight from end of else to start of if and the code surround by try catch
        } catch (e: Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}
