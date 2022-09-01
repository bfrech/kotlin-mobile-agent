package com.example.kotlin_agent.activities


import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Toast
import com.example.kotlin_agent.AgentService
import com.example.kotlin_agent.R
import com.example.kotlin_agent.store.AndroidFileSystemUtils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanIntentResult
class ContactsActivity : AppCompatActivity() {


    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("connection-invitation")
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(completedConnectionMessageReceiver,
            IntentFilter("connection_completed")
        )

        // Button to create a new Invitation
        val addContactButton: FloatingActionButton = findViewById(R.id.addContactActionButton)
        addContactButton.setOnClickListener {
            val service = Intent(this, AgentService::class.java)
            service.action = "createInvitation"
            startService(service)
        }

        // Button to scan an Invitation
        val scanQrActionButton: FloatingActionButton = findViewById(R.id.scanQRActionButton)
        scanQrActionButton.setOnClickListener{
            val options = ScanOptions()
            options.setPrompt("Scan a QR Code")
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            barcodeLauncher.launch(options)
        }


        // Display Contacts List
        val listView: ListView = findViewById(R.id.listview_contacts)
        val contacts = getContactList()
        adapter = ArrayAdapter(this, R.layout.contacts_item, contacts.toTypedArray())
        listView.adapter = adapter

        // Open Messaging Screen for Label
        listView.setOnItemClickListener { parent, _, position, _ ->
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("Label",parent.getItemAtPosition(position).toString() )
            startActivity(intent)
        }
    }

    // Barcode
    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
            println("Result: ${result.contents}")
            acceptInvitationFromQRCode(result.contents)
        }
    }

    private fun acceptInvitationFromQRCode(didDocEncoded: String){
        val service = Intent(this, AgentService::class.java)
        service.putExtra("qr_invitation", didDocEncoded)
        service.action = "acceptInvitation"
        startService(service)
    }

    // Get Contacts from SharedPrefs
    private fun getContactList(): MutableSet<String> {
        return AndroidFileSystemUtils.getContactList(this)
    }


    // Handler for received Intents. This will be called whenever an Intent
    // with an action named "connection-invitation" is broadcasted.
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra("message")

            // Go to invitation Screen
            val invitationIntent = Intent(this@ContactsActivity, NewInvitationActivity::class.java)
            invitationIntent.putExtra("new_invitation",message)
            startActivity(invitationIntent)
        }
    }

    private val completedConnectionMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            adapter.notifyDataSetChanged()
            val contactsIntent = Intent(this@ContactsActivity, ContactsActivity::class.java)
            startActivity(contactsIntent)

        }
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(completedConnectionMessageReceiver)
        super.onDestroy()
    }

}