package com.example.kotlin_agent

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class AgentService: Service(){

    var ariesAgent = AriesAgent(this)
    private val TAG = "AgentService"

    /*
        Android Service Functions
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")

        if (intent != null) {
            val action = intent.action
            Log.d(TAG, "onStartCommand executed with action: $action")

            if (action.equals("startAgent")) {
                val extras = intent.extras
                if (extras == null) {
                    Log.e(TAG, "No mediator URL or Label given")
                } else {

                    // If Agent already started, only reconnect to mediator
                    if (ariesAgent.ariesAgent == null) {
                        object: Thread(){
                            override fun run(){
                                ariesAgent.connectToMediator(extras["mediatorURL"].toString())
                            }
                        }.start()
                    }
                    object: Thread(){
                        override fun run(){
                            ariesAgent.createNewAgent(extras["label"].toString())
                            ariesAgent.connectToMediator(extras["mediatorURL"].toString())
                        }
                    }.start()
                }
            }

            if (action.equals("createInvitation")) {
                val invitation = ariesAgent.createConnectionInvitation()
                sendCreatedInvitationMessage(invitation)
            }

            if (action.equals("acceptInvitation")) {
                val extras = intent.extras
                if (extras == null) {
                    println("No Value was given")
                } else {
                    val invitation = extras["qr_invitation"].toString()
                    ariesAgent.createAndSendConnectionRequest(invitation)
                }
            }


            if (action.equals("writeMessage")) {
                val extras = intent.extras
                if (extras == null) {
                    Log.e(TAG, "No Value for message was given")
                } else {
                    val message = extras["message"].toString()
                    val connectionID = extras["connectionID"].toString()
                    ariesAgent.sendMessage(message, connectionID)
                }
            }


        } else {
            Log.d(
                TAG, "with a null intent. It has been probably restarted by the system."
            )
        }

        // Makes sure that the service is restarted if the system kills the service
        return START_STICKY
    }


    private fun sendCreatedInvitationMessage(peerDIDDocEncoded: String) {
        val intent = Intent("connection-invitation")
        intent.putExtra("message", peerDIDDocEncoded)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}