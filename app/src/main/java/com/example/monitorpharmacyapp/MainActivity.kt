package com.example.monitorpharmacyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar

class MainActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun login(view: View) {
        var etEmail = findViewById<EditText>(R.id.etEmail)
        var etPassword = findViewById<EditText>(R.id.etPassword)

        if (etEmail.text.isNotEmpty() && etPassword.text.toString() == "12345"){
            var intent = Intent(applicationContext,MonitorInterface::class.java)
            startActivity(intent)
        }else{
            Toast.makeText(this,"Invalid username or password",Toast.LENGTH_SHORT).show()
        }
    }
}