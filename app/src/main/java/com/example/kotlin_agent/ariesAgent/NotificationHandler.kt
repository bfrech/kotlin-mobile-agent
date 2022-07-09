package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.api.Handler
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class NotificationHandler(private val ariesAgent: AriesAgent) : Handler {

    override fun handle(topic: String, message: ByteArray) {
        println("received notification topic: $topic")
        println("received notification message: " + String(message, StandardCharsets.UTF_8))



        val json = JSONObject(String(message, StandardCharsets.UTF_8))
        val jsonMessage = JSONObject(json["message"].toString())

        if(topic == "basicmessage"){
            // TODO: do something with message
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