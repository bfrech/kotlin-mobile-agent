package com.example.kotlin_agent

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class AgentService: Service(){

    var ariesAgent = AriesAgent(this)

    /*
        Android Service Functions
     */
    override fun onBind(intent: Intent?): IBinder? {

        println("Some component wants to bind this service")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand executed with startId: $startId")

        if (intent != null) {
            val action = intent.action
            println("using an intent with action $action")

            if (action.equals("startAgent")) {
                val extras = intent.extras
                if (extras == null) {
                    println("No mediator URL or Label given")
                } else {

                    val backgroundSetup = object: Thread(){
                        override fun run(){
                            ariesAgent.createNewAgent(extras["label"].toString())
                            ariesAgent.connectToMediator(extras["mediatorURL"].toString())
                        }
                    }.start()

                }

            }

            if (action.equals("createInvitation")) {
                val invitation = ariesAgent.createConnectionInvitation()
                println("Created Invitation: $invitation")

                if (invitation != null) {
                    sendPeerDidMessage(invitation)
                }
            }

            if (action.equals("acceptInvitation")) {
                val extras = intent.extras
                if (extras == null) {
                    println("No Value was given")
                } else {
                    var invitation = extras["did"].toString()
                    println("Got Invitation: $invitation")
                    ariesAgent.createAndSendConnectionRequest(invitation)
                }
            }

        } else {
            println(
                "with a null intent. It has been probably restarted by the system."
            )
        }

        // Makes sure that the service is restarted if the system kills the service
        return START_STICKY
    }


    private fun sendPeerDidMessage(peerDIDDocEncoded: String) {
        println("sender: Broadcasting message")
        val intent = Intent("created-peer-did")
        intent.putExtra("message", peerDIDDocEncoded)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}