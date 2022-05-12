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
        val properties = JSONObject(jsonMessage["Properties"].toString())

        if(jsonMessage["StateID"].equals("completed")) {

            if(properties["connectionID"].equals(ariesAgent.routerConnectionId)){
                println("Reached completed State: register Router now")
                ariesAgent.registerMediator()
            }
        } else if(jsonMessage["StateID"].equals("requested")){
            val message = JSONObject(jsonMessage["Message"].toString())

            // TODO: Replace with regex
            if(message["@type"].equals("https://didcomm.org/didexchange/1.0/request")){
                ariesAgent.acceptDIDExchangeRequest(properties["connectionID"].toString(), message["label"].toString())
            }
        }
    }
}