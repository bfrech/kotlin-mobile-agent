package com.example.kotlin_agent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kotlin_agent.ariesAgent.AriesAgentService

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)


        val addContactButton: Button = findViewById(R.id.addContactActionButton)
        addContactButton.setOnClickListener {

            // TODO: Need Android Service that is accessible from all Activities
            // service.createOOBInvitationForMobileAgent()
        }

    }
}