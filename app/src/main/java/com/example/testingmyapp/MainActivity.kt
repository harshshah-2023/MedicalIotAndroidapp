package com.example.testingmyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.example.testingmyapp.R

class MainActivity : AppCompatActivity() {
    private lateinit var bnView: BottomNavigationView
//val bnView:BottomNavigationView=findViewById(R.id.bnView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bnView = findViewById(R.id.bnView)

        if(savedInstanceState==null){
            replacefrga(HomeeFragment())
        }
//        val bnView:BottomNavigationView=findViewById(R.id.bnView)
        bnView.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.nav_home->{
                    replacefrga(HomeeFragment())
                    true
                }

                R.id.nav_explore->{
                    replacefrga(CameraSetupFragment())
                    true
                }

                R.id.nav_chat->{
                    replacefrga(MedicineSetupFragment())
                    true
                }

                R.id.nav_profile->{
                    replacefrga(MyaccountFragment())
                    true
                }
                else->false
            }
        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun replacefrga(fragment:Fragment){
        val fragmentManager:FragmentManager=supportFragmentManager

        val transaction:FragmentTransaction=fragmentManager.beginTransaction()

        transaction.replace(R.id.container1,fragment)

        transaction.commit()
    }


}
