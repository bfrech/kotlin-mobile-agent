package com.example.kotlin_agent.ariesAgent

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaParser.SeekPoint.START
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.google.firebase.appindexing.builders.Actions
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.io.Serializable
import java.net.URL
import java.nio.charset.StandardCharsets

class AriesAgentService: Service(){

    var ariesAgent: AriesController? = null
    var useLocalAgent: Boolean = true
    var agentlabel: String = ""
    var routerConnectionId = ""
    var mediatorURL = ""

    var mediatorService: MediatorService = MediatorService(this)
    var connectionService: ConnectionService = ConnectionService(this)


    private val backgroundSetup = object: Thread(){
        override fun run(){
            createNewAgent(agentlabel)
            connectToMediator(mediatorURL)
        }
    }.start()

    private fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = useLocalAgent
        opts.transportReturnRoute = "all"
        opts.label = label
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"
        //opts.autoAccept = true  --> default value?

        try {
            ariesAgent = Ariesagent.new_(opts)
            val handler = ConnectionHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered handler with registration id: $registrationID")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    private fun connectToMediator(mediatorUrl: String){
        mediatorService.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediatorService.registerMediator()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBV2InvitationForMobileAgent() {
        connectionService.createOOBV2InvitationForMobileAgent("Connect", "connect")
    }

    fun createOOBInvitationForMobileAgent() {
        connectionService.createOOBInvitationForMobileAgent()
    }

    fun createDIDExchangeRequest() {
        connectionService.createDIDExchangeRequest()
    }


    /*
        Android Service Functions
     */
    override fun onBind(intent: Intent?): IBinder? {
        println("Some component wants to bind this service")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand executed with startId: $startId")

        if (intent != null){
            val action = intent.action
            println("using an intent with action $action")

            if (action.equals("startAgent")) {
                var extras = intent?.extras
                if (extras == null){
                    println("No mediator URL or Label given")
                } else {
                    mediatorURL= extras["mediatorURL"].toString()
                    agentlabel = extras["label"].toString()
                    println(mediatorURL)
                }
                val setup = backgroundSetup
            }

            if (action.equals("createInvitation")){
                createOOBInvitationForMobileAgent()
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