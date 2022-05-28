package com.example.kotlin_agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("created-peer-did")
        )

        val addContactButton: FloatingActionButton = findViewById(R.id.addContactActionButton)
        addContactButton.setOnClickListener {
            val service = Intent(this, AgentService::class.java)
            service.action = "createInvitation"
            startService(service)
        }

        val scanQrActionButton: FloatingActionButton = findViewById(R.id.scanQRActionButton)
        scanQrActionButton.setOnClickListener{
            val service = Intent(this, AgentService::class.java)
            service.action = "acceptInvitation"
            startService(service)

            // TODO: Accept Invitation Screen
        }
    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "created-peer-did" is broadcasted.
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra("message")
            println("Receiver Got message: $message")

            // Go to invitation Screen
            val intent = Intent(this@ContactsActivity, NewInvitationActivity::class.java)
            intent.putExtra("peerDID",message)
            startActivity(intent)

        }
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

}