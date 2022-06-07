package com.example.kotlin_agent.ariesAgent

import org.json.JSONObject
import java.nio.charset.StandardCharsets

class DidExchange(private val service: AriesAgent) {

    fun createDidExchangeInvitation(): String {
        val payload = """ {"alias": "${service.agentlabel}", "router_connection_id": "${service.routerConnectionId}", "opts": {"router_connections": "${service.routerConnectionId}", "keyType": "X25519ECDHKW"}} """
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println(actionsResponse)
                return actionsResponse
            }
        }
        return ""
    }

    fun receiveDidExchangeInvitation(invitation: String): String {
        val jsonInvitation = JSONObject(invitation)
        val payload = jsonInvitation["invitation"].toString()
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.receiveInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonResponse = JSONObject(actionsResponse)
                println(jsonResponse["connection_id"])
                return jsonResponse["connection_id"].toString()
            }
        }
        return ""
    }

    fun acceptDidExchangeInvitation(connectionID: String): String {

        val payload = """ {"id":"$connectionID", "router_connections": "${service.routerConnectionId}"} """

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.acceptInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonResponse = JSONObject(actionsResponse)
                println(jsonResponse["connection_id"])
                return jsonResponse["connection_id"].toString()
            }
        }
        return ""
    }


    fun acceptDidExchangeRequest(connectionID: String): String {
        val payload = """ {"id":"$connectionID", "router_connections": "${service.routerConnectionId}"} """
        println("Accept DID Exchange Request Payload: $payload")
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.acceptExchangeRequest(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                //val jsonResponse = JSONObject(actionsResponse)
                //println(jsonResponse["connection_id"])
                println(actionsResponse)
                return ""
            }
        }
        return ""
    }
}