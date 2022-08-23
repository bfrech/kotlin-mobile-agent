package com.example.kotlin_agent.activities


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.kotlin_agent.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter



class NewInvitationActivity : AppCompatActivity() {

    lateinit var qrImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_invitation)

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("connection_completed")
        )

        // Render Invitation as QR Code
        qrImageView = findViewById(R.id.qrCodeImage)
        val invitation = intent.getStringExtra("new_invitation")
        val bitmap = invitation?.let { getQrCodeBitmap(it) }
        qrImageView.setImageBitmap(bitmap)
    }


    private fun getQrCodeBitmap(qrCodeContent: String): Bitmap {
        val size = 512
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }


    // Handler for received Intents. This will be called whenever an Intent
    // with an action named "connection_completed" is broadcasted.
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra("message")
            println("Receiver Got message: $message")

            // Go to invitation Screen
            val intent = Intent(this@NewInvitationActivity, ContactsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }


}