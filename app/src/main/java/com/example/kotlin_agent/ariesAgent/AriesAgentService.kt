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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand executed with startId: $startId")

        if (intent != null){
            val action = intent.action
            println("using an intent with action $action")
            println(action.equals("startAgent"))

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
                AriesAgent.getInstance()?.createOOBInvitationForMobileAgent()
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