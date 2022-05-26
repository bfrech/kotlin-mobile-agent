package com.example.kotlin_agent

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import com.example.kotlin_agent.didcomm.DIDCommAgent

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

        if (intent != null){
            val action = intent.action
            println("using an intent with action $action")

            if (action.equals("startAgent")) {
                val extras = intent.extras
                if (extras == null){
                    println("No mediator URL or Label given")
                } else {
                    mediatorURL= extras["mediatorURL"].toString()
                    agentlabel = extras["label"].toString()
                    val setup = backgroundSetup
                }

            }

            if (action.equals("createInvitation")){
                //AriesAgent.getInstance()?.createOOBInvitationForMobileAgent()
                DIDCommAgent.getInstance()?.createPeerDID()
            }

            if (action.equals("acceptInvitation")){
                val did = "did:peer:2.Ez6LSnkzXAqNYCbKTBCwsK3Puw9Gk91PJAQqepAbF3Co3DVke.Vz6MkuPv5x2oPJ9KQvwedzKg7dyXwDBqcUDq2wM2a6H6wAefX.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJ3czovL01CUC12b24tQmVyaXQuZnJpdHouYm94OjUwMDEiLCJyIjpbIlwiZGlkOmtleTp6NkxTclozREs5Nk1xZ1dpVnRSclN6QlU4aWIyNmJ5d0VLMkJpRmNuZ0NqRnQ5TXdcIiJdLCJhIjpbImRpZGNvbW0vdjIiXX0"
                DIDCommAgent.getInstance()?.acceptPeerDIDInvitation(theirDID = did, name="Bob")
            }

            if (action.equals("receiveInvitation")){
                //DIDCommAgent.getInstance()?.acceptPeerDIDInvitation("...")
            }



        } else {
            println(
                "with a null intent. It has been probably restarted by the system."
            )
        }

        // Makes sure that the service is restarted if the system kills the service
        return START_STICKY
    }


}