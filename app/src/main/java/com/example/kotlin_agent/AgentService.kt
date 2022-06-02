package com.example.kotlin_agent

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import com.example.kotlin_agent.didcomm.DIDCommAgent
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class AgentService: Service(){

    var agentlabel: String = ""
    var mediatorURL = ""


    private val backgroundSetup = object: Thread(){
        override fun run(){
            AriesAgent.getInstance()?.createNewAgent(agentlabel)
            AriesAgent.getInstance()?.connectToMediator(mediatorURL)
        }
    }.start()


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
                    mediatorURL = extras["mediatorURL"].toString()
                    agentlabel = extras["label"].toString()
                    val setup = backgroundSetup
                }

            }

            if (action.equals("createInvitation")) {
                val myDIDDocEncoded = DIDCommAgent.getInstance()?.createPeerDID()
                println("Created MyDIDDocEncoded: $myDIDDocEncoded")

                // Subscribe to Message to wait for response
                AriesAgent.getInstance()?.registerService("invitation-response", "complete-invitation")

                if (myDIDDocEncoded != null) {
                    sendPeerDidMessage(myDIDDocEncoded)
                }
            }

            if (action.equals("acceptInvitation")) {
                val extras = intent.extras
                if (extras == null) {
                    println("No Value was given")
                } else {
                    val did = extras["did"].toString()
                    val myDIDDocEncoded = DIDCommAgent.getInstance()
                        ?.acceptPeerDIDInvitation(theirDIDDocEncoded = did, name = "Bob")

                    println("Accepted Invitation with myAriesDIDDocEncoded: $myDIDDocEncoded")

                    // TODO: Broadcast Message: "accepted-invitation" and trigger message to other agent with myDID
                    if (myDIDDocEncoded != null) {
                        sendAcceptedInvitationMessage(myDIDDocEncoded)
                    }



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

    private fun sendAcceptedInvitationMessage(myDid: String) {
        println("sender: Broadcasting message")
        val intent = Intent("accepted-invitation")
        intent.putExtra("message", myDid)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}