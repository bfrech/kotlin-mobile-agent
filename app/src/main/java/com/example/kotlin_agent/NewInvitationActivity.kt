package com.example.kotlin_agent

import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context


class NewInvitationActivity : AppCompatActivity() {

    lateinit var qrImageView: ImageView
    val did = "did:peer:2.Ez6LSiEp8B3VwSnmp3yZGkigQE8NwaYQNZNEp1MhG9xtapFsC.Vz6MkquHnx5Rrj3P2ZywFF9LZXftvEHaSgq46SRhkZxgiEhwq.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJ3czovL01CUC12b24tQmVyaXQ6NTAwMSIsInIiOlsiXCJkaWQ6a2V5Ono2TFNqdTRmZFZVVDRyZEI0YWVKd2NUTk02aG05MkdoRWY4VkNOMWc3MXprRXZRYVwiIl0sImEiOlsiZGlkY29tbS92MiJdfQ"


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_invitation)

        // Render PeerDID as QR Code
        qrImageView = findViewById(R.id.qrCodeImage)
        val peerDID = intent.getStringExtra("peerDID")
        val bitmap = peerDID?.let { getQrCodeBitmap(it) }
        qrImageView.setImageBitmap(bitmap)
    }


    private fun getQrCodeBitmap(qrCodeContent: String): Bitmap {
        val size = 512 //pixels
        //val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

}