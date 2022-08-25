package com.example.kotlin_agent.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlin_agent.activities.messages.MessageListAdapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.kotlin_agent.AgentService

import com.example.kotlin_agent.R
import com.example.kotlin_agent.Utils
import com.example.kotlin_agent.store.AndroidFileSystemUtils


class MessageActivity : AppCompatActivity() {

    private lateinit var mMessageRecycler: RecyclerView
    private lateinit var mMessageAdapter: MessageListAdapter
    private lateinit var theirLabel: String
    private lateinit var connectionID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        LocalBroadcastManager.getInstance(this).registerReceiver(newMessageMessageReceiver,
            IntentFilter("received_new_message")
        )

        val extras = intent.extras
        if (extras != null) {
            theirLabel = extras.getString("Label").toString()
            connectionID = AndroidFileSystemUtils.getConnectionIDForLabel(this, theirLabel).toString()
            println("Starting Messaging Activity for $theirLabel with ID: $connectionID")

            val messageList = AndroidFileSystemUtils.getMessagesFromSharedPrefs(this, theirLabel)

            mMessageRecycler = findViewById<View>(R.id.recycler_messaging) as RecyclerView
            mMessageAdapter = MessageListAdapter(messageList)
            mMessageRecycler.layoutManager = LinearLayoutManager(this)
            mMessageRecycler.adapter = mMessageAdapter
        }

        // Send Button
        val sendButton = findViewById<Button>(R.id.button_send)
        val messageEdit = findViewById<EditText>(R.id.edit_message)
        val ariesService = Intent(this, AgentService::class.java)

        sendButton.setOnClickListener {
            val message = messageEdit.text.toString()
            AndroidFileSystemUtils.storeMessageToSharedPrefs(this, message, true ,theirLabel)
            ariesService.putExtra("message", message)
            ariesService.putExtra("connectionID", connectionID)
            ariesService.action = "writeMessage"
            startService(ariesService)

            // display message
            updateAdapter()
            messageEdit.text.clear()
        }
    }


    private fun updateAdapter(){
        val messageList = AndroidFileSystemUtils.getMessagesFromSharedPrefs(this, theirLabel)
        mMessageAdapter.updateMessageList(messageList)
    }

    private val newMessageMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            updateAdapter()

        }
    }
}