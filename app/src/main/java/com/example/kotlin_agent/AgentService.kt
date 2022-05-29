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
                val myDID = DIDCommAgent.getInstance()?.createPeerDID()
                println("Created MyDID: $myDID")
                if (myDID != null) {
                    sendPeerDidMessage(myDID)
                }
            }

            if (action.equals("acceptInvitation")) {

                // Only for testing:
                val did =
                    "did:peer:2.Ez6LSiEp8B3VwSnmp3yZGkigQE8NwaYQNZNEp1MhG9xtapFsC.Vz6MkquHnx5Rrj3P2ZywFF9LZXftvEHaSgq46SRhkZxgiEhwq.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJ3czovL01CUC12b24tQmVyaXQ6NTAwMSIsInIiOlsiXCJkaWQ6a2V5Ono2TFNqdTRmZFZVVDRyZEI0YWVKd2NUTk02aG05MkdoRWY4VkNOMWc3MXprRXZRYVwiIl0sImEiOlsiZGlkY29tbS92MiJdfQ"

                // TODO: Get DID and Name from Extras

                val myDID = DIDCommAgent.getInstance()
                    ?.acceptPeerDIDInvitation(theirDID = did, name = "Bob")
                println("Accepted Invitation with myDID: $myDID")
            }


        } else {
            println(
                "with a null intent. It has been probably restarted by the system."
            )
        }

        // Makes sure that the service is restarted if the system kills the service
        return START_STICKY
    }


    private fun sendPeerDidMessage(peerDID: String) {
        println("sender: Broadcasting message")
        val intent = Intent("created-peer-did")
        intent.putExtra("message", peerDID)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}