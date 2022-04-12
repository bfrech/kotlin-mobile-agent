package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.StandardCharsets

class MediatorService(private val service: AriesAgentService) {

    fun connectToMediator(mediatorUrl: String){
        val url = URL(mediatorUrl)
        val connection = url.openConnection()

        val content = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
        val invite = """{ "invitation": $content, "my_label": "${service.agentlabel}" }"""

        var res: ResponseEnvelope
        try {
            val outOfBandController = service.ariesAgent?.outOfBandController
            val data = invite.toByteArray(StandardCharsets.UTF_8)
            if (outOfBandController != null) {
                res = outOfBandController.acceptInvitation(RequestEnvelope(data))
                if(res.error != null){
                    println(res.error.message)
                } else {
                    val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                    val jsonActionResponse = JSONObject(actionsResponse)
                    service.routerConnectionId = jsonActionResponse["connection_id"].toString()
                    println("""Accepted Router Invitation with: ${service.routerConnectionId}""")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun registerMediator() {
        val mediatorController = service.ariesAgent?.mediatorController
        val mediatorRequest = """ {"connectionID":"${service.routerConnectionId}"} """

        val data = mediatorRequest.toByteArray(StandardCharsets.UTF_8)
        val res = mediatorController?.register(data)

        if (res != null) {
            if(res.error != null){
                println("There was an error with the Router Registration ${res.error.message}")
            } else {
                println("Registered Router with: ${service.routerConnectionId}")
            }
        }
    }

}