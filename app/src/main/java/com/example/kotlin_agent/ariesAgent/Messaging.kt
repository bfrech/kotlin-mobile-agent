package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.RequestEnvelope
import java.nio.charset.StandardCharsets

class Messaging(private val service: AriesAgent) {

    /*
       Register Service
     */
    fun registerMessagingService(name: String, purpose: String) {
        val messageController = service.ariesAgent?.messagingController
        val payload =
            """{"name":"$name", "purpose": ["$purpose"], "type": "https://didcomm.org/generic/1.0/message"} """
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.registerService(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
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
    fun sendConnectionMessage(message: String, connectionID: String, purpose: String) {
        val messageController = service.ariesAgent?.messagingController
        val messageBody = """ {
			    "@type": "https://didcomm.org/connection/2.0/message",
                "purpose": "$purpose",
                "body": {
			        "did_doc": "$message",
                    "label": "${service.agentlabel}"
                }
			} """
        val payload = """ {"message_body": $messageBody, "connection_id": "$connectionID"} """
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


    fun sendMessageViaServiceEndpoint(message: String, serviceEndpoint: String, purpose: String) {
        val messageController = service.ariesAgent?.messagingController
        val messageBody = """ {
			    "@type": "https://didcomm.org/connection/2.0/message",
                "purpose": "$purpose",
                "body": {
			        "did_doc": "$message",
                    "label": "${service.agentlabel}"
                }
			} """
        val payload = """ {"message_body": $messageBody, "service_endpoint": $serviceEndpoint} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            }
        }
    }




    /*
        Reply to message?
     */

}
