package com.example.kotlin_agent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    lateinit var connectButton: Button
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
            ariesService.putExtra("mediatorURL","http://bb86-193-175-2-18.eu.ngrok.io/invitation")
            //service.putExtra("mediatorURL",mediatorURLEdit.text.toString())

            ariesService.putExtra("label",labelEdit.text.toString())
            ariesService.action = "startAgent"
            startService(ariesService)

            // Screen 2
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }





}