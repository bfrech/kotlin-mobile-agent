package com.example.kotlin_agent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kotlin_agent.ariesAgent.AriesAgentService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)


        val addContactButton: FloatingActionButton = findViewById(R.id.addContactActionButton)
        addContactButton.setOnClickListener {

            val service = Intent(this, AriesAgentService::class.java)
            service.action = "createInvitation"
            startService(service)

        }

    }
}