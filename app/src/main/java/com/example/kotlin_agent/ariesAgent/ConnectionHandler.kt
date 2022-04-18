package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.api.Handler
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class ConnectionHandler(private val ariesAgentService: AriesAgentService) : Handler {

    override fun handle(topic: String, message: ByteArray) {
        println("received notification topic: $topic")
        println("received notification message: " + String(message, StandardCharsets.UTF_8))

        val json = JSONObject(String(message, StandardCharsets.UTF_8))
        val jsonMessage = JSONObject(json["message"].toString())
        if(jsonMessage["StateID"].equals("completed")) {
            val message = JSONObject(jsonMessage["Properties"].toString())

            if(message["connectionID"].equals(ariesAgentService.routerConnectionId)){
                println("Reached completed State: register Router now")
                ariesAgentService.registerMediator()
            }

        }
    }
}