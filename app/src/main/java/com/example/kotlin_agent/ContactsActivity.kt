package com.example.kotlin_agent


import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanIntentResult
class ContactsActivity : AppCompatActivity() {

    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_sharedPreferences",
            Context.MODE_PRIVATE
        )
    }


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
            val options = ScanOptions()
            options.setPrompt("Scan a QR Code")
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            barcodeLauncher.launch(options)
        }


        // Display Contacts List
        val values: MutableMap<String, *> = sharedPref.all
        val contacts = values.keys
        val adapter = ArrayAdapter(this, R.layout.contacts_item, contacts.toTypedArray())
        val listView: ListView = findViewById(R.id.listview_contacts)
        listView.adapter = adapter
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
        service.putExtra("did", didDocEncoded)
        service.action = "acceptInvitation"
        startService(service)
    }


    // Handler for received Intents. This will be called whenever an Intent
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