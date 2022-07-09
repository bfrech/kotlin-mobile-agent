package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.RequestEnvelope
import java.nio.charset.StandardCharsets

class Messaging(private val service: AriesAgent) {

    /*
       Register Service
     */
    fun registerMessagingService(name: String, purpose: String){
        val messageController = service.ariesAgent?.messagingController
        val payload = """{"name":"generic-invite", "purpose": ["meeting","appointment","event"], "type": "https://didcomm.org/generic/1.0/message"} """
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.registerService(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {

                // Should return empty json
                //val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("Registered new Service!")
            }
        }

        getRegisteredServices()
    }

    fun getRegisteredServices() {
        val messageController = service.ariesAgent?.messagingController
        val payload = """ {} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.services(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {

                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("Registered Services: $actionsResponse")
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


    fun sendMessageViaServiceEndpoint(message: String, serviceEndpoint: String){
        val messageController = service.ariesAgent?.messagingController
        val messageBody = """ {
			    "@type": "https://didcomm.org/basicmessage/2.0/message",
                "lang": "en",
                "body": {
			        "content": "Hallo"
                }
			} """
        val payload = """ {"message_body": $messageBody, "service_endpoint": $serviceEndpoint} """
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


    fun sendMessageViaTheirDID(message: String, theirDID: String){
        val messageController = service.ariesAgent?.messagingController
        val payload = """ {"message_body": {"text":"$message", "type": "https://didcomm.org/generic/1.0/message", "purpose": "event"}, "their_did": "$theirDID"} """
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