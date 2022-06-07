package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.RequestEnvelope
import java.nio.charset.StandardCharsets

class Messaging(private val service: AriesAgent) {

    //private val messageController = service.ariesAgent?.messagingController

    /*
       Register Service
     */
    fun registerMessagingService(name: String, purpose: String){
        val messageController = service.ariesAgent?.messagingController
        val payload = """{"name":"$name", "purpose": ["$purpose"]}"""
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.registerHTTPService(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {

                // Should return empty json
                println("Registered new Service! ${res.payload}")
            }
        }
    }

    /*
        Send Message
     */
    fun sendMessage(message: String, connectionID: String){
        val messageController = service.ariesAgent?.messagingController
        val payload = """ {"message_body": {"text":"$message"}, "connection_id": "$connectionID"} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)

                // Should return empty json
                println(actionsResponse)
            }
        }
    }

    /*
        Reply to message?
     */

}