package com.example.kotlin_agent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val addContactButton: FloatingActionButton = findViewById(R.id.addContactActionButton)
        addContactButton.setOnClickListener {

            val service = Intent(this, AgentService::class.java)
            service.action = "createInvitation"
            startService(service)

        }

    }
}