package com.example.kotlin_agent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.kotlin_agent.ariesAgent.AriesAgentService
import com.example.kotlin_agent.didcomm.DIDCommService

class MainActivity : AppCompatActivity() {

    lateinit var connectButton: Button
    lateinit var mediatorURLEdit: EditText
    lateinit var labelEdit: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediatorURLEdit = findViewById(R.id.mediatorURLeditText)
        labelEdit = findViewById(R.id.yourLabelEditText)

        val service = Intent(this, AriesAgentService::class.java)

        connectButton = findViewById(R.id.connectbutton)
        connectButton.setOnClickListener {

            service.putExtra("mediatorURL",mediatorURLEdit.text.toString())
            service.putExtra("label",labelEdit.text.toString())
            service.action = "startAgent"
            startService(service)

            // Screen 2
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }





}