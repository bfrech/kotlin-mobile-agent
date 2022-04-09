package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.RequestEnvelope
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class ConnectionService(private val service: AriesAgentService) {


    fun createInvitationForMobileAgent(goal: String, goalCode: String) {

        // S1: DID of this Agent? -> or empty
        val s1 = null
        val protocols = "https://didcomm.org/didexchange/1.0"

        val oobController = service.ariesAgent?.outOfBandController
        val payload = """{"label":"${service.agentlabel}","goal":"$goal","goal_code":"$goalCode","service":["$s1"],"protocols":["$protocols"], "router_connection_id":"${service.routerConnectionId}"}"""
        val data = payload.toByteArray(StandardCharsets.UTF_8)

        println(payload)

        val invitation = oobController?.createInvitation(RequestEnvelope(data))
        println(invitation)
    }
}