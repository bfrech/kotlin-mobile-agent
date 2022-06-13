package com.example.kotlin_agent

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject


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
                val invitation = AriesAgent.getInstance()?.createDidExchangeInvitation()
                println("Created Invitation: $invitation")

                // Subscribe to Message to wait for response
                AriesAgent.getInstance()?.registerService("invitation-response", "complete-invitation")

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
                    println(invitation)

                    // Create Their DID and My DID and store connection
                    //val connectionID = AriesAgent.getInstance()?.acceptConnectionInvitation(invitation)
                    //println("Accepted Invitation with Connection ID: $connectionID")

                    val connectionID = ""
                    // "accepted-invitation" and trigger message to other agent with myDID
                    if (connectionID != null) {
                        sendAcceptedInvitationMessage(connectionID)

                        // If OK: send message to other agent
                        //AriesAgent.getInstance()?.sendMessage("invitation-response", connectionID)


                    }

                    // TODO: send message via service endpoint
                    //val jsonInvitation = JSONObject(invitation)
                    //val serviceEndpoint = jsonInvitation["invitation"].toString()

                    //invitation = """ {"serviceEndpoint":"ws://MBP-von-Berit.fritz.box:5001","recipientKeys":["did:key:z6LSmZvhsAK43vau5w6VZUV6YnhBnmyZysZCpmn6p9Ys362b"],"@id":"80fb0820-9a5a-4f5f-8c39-c0a80c3d6f9b","routingKeys":["did:key:z6LSdHFSbQGstPn3eKFmYMy5pXRugjp2TA9PT7G2DEQ6YDRb"],"@type":"https://didcomm.org/didexchange/1.0/invitation"} """

                    AriesAgent.getInstance()?.sendMessageViaServiceEndpoint("invitation-response", invitation)

                    // TODO: Save Connection ID and Name in Store

                }
            }


            if (action.equals("completeInvitation")) {
                val extras = intent.extras
                if (extras == null) {
                    println("No Value was given")
                } else {
                    val invitation = extras["did"].toString()

                    // Create Their DID and My DID and store connection
                    val connectionID = AriesAgent.getInstance()?.acceptConnectionInvitation(invitation)
                    println("Completed Invitation with Connection ID: $connectionID")

                    // "completed-invitation" and trigger message to other agent with connectionID
                    if (connectionID != null) {
                        sendCompletedInvitationMessage(connectionID)
                    }

                    // TODO: Save Connection ID and Name in Store
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

    private fun sendCompletedInvitationMessage(myDid: String) {
        println("sender: Broadcasting message")
        val intent = Intent("accepted-invitation")
        intent.putExtra("message", myDid)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}