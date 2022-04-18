package com.example.kotlin_agent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.kotlin_agent.ariesAgent.AriesAgentService
import com.example.kotlin_agent.didcomm.DIDCommService

class MainActivity : AppCompatActivity() {

    var service: AriesAgentService? = AriesAgentService()

    lateinit var connectButton: Button
    lateinit var mediatorURLEdit: EditText
    lateinit var labelEdit: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connectbutton)
        connectButton.setOnClickListener {

            // Call Setup Agent with mediator URL and Label in background
            val background = object: Thread(){
                override fun run(){
                    mediatorURLEdit = findViewById(R.id.mediatorURLeditText)
                    labelEdit = findViewById(R.id.yourLabelEditText)
                    service?.setupAgentWithLabelAndMediator(mediatorURLEdit.text.toString(), labelEdit.text.toString())
                }
            }.start()

            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }





}