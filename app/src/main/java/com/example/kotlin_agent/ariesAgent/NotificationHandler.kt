package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.api.Handler
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class NotificationHandler(private val ariesAgent: AriesAgent) : Handler {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun handle(topic: String, message: ByteArray) {
        println("received notification topic: $topic")
        println("received notification message: " + String(message, StandardCharsets.UTF_8))



        val json = JSONObject(String(message, StandardCharsets.UTF_8))
        val jsonMessage = JSONObject(json["message"].toString())

        if(topic == "connection"){
            // TODO: Connection Request Handling (call service with complete connection)
            println("Recieved Connection Request: $jsonMessage" )
            ariesAgent.createAndSendConnectionResponse(jsonMessage["DIDDoc"].toString(),jsonMessage["label"].toString())
            return
        }

        if(topic == "basicmessage"){
            // TODO: do something with message
            println("Got a Message: $jsonMessage")
            return
        }

        val properties = JSONObject(jsonMessage["Properties"].toString())
        if(jsonMessage["StateID"].equals("completed")) {
            if(properties["connectionID"].equals(ariesAgent.routerConnectionId)){
                println("Reached completed State: register Router now")
                ariesAgent.registerMediator()
            }
        }
    }
}