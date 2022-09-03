package com.example.kotlin_agent.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.kotlin_agent.AgentService
import com.example.kotlin_agent.BuildConfig
import com.example.kotlin_agent.R

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    lateinit var mediatorURLEdit: EditText
    lateinit var labelEdit: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            mediatorURLEdit = findViewById(R.id.mediatorURLeditText)
            labelEdit = findViewById(R.id.yourLabelEditText)

            val ariesService = Intent(this, AgentService::class.java)

            connectButton = findViewById(R.id.connectbutton)
            connectButton.setOnClickListener {

                // TODO: TEST Case
                ariesService.putExtra("mediatorURL","http://c13c-2003-e6-f46-3a01-dab-ad95-270d-a0cf.eu.ngrok.io")
                //service.putExtra("mediatorURL",mediatorURLEdit.text.toString())

                ariesService.putExtra("label",labelEdit.text.toString())
                ariesService.action = "startAgent"
                startService(ariesService)

                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
            }
        }
    }





