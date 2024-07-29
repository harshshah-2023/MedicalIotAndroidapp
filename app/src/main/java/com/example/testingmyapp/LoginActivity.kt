package com.example.testingmyapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    fun createforgetbtn(view: View) {
        val aditi= Intent(this,MainActivity::class.java)
        startActivity(aditi)
    }

    fun forgetbncreate(view: View) {
        val passtofrg = Intent(this,ForegtpasswordActivity::class.java)
        startActivity(passtofrg)
    }

    fun createSignup(view: View) {
       val pastoreg = Intent(this,RegistrationActivity::class.java)
        startActivity(pastoreg)

        }
    }



