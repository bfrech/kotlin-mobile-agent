package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.*

class ConnectionService(private val service: AriesAgentService) {


    @RequiresApi(Build.VERSION_CODES.O)
    fun createInvitationForMobileAgent(goal: String, goalCode: String) {

        // S1: DID of this Agent? -> or empty
        //val s1 = null
        //val protocols = "https://didcomm.org/didexchange/1.0"

        val didExchangeRequest =
            """{"alias": "${service.agentlabel}", "router_connection_id":"${service.routerConnectionId}"}"""
        val didExchangeData = didExchangeRequest.toByteArray(StandardCharsets.UTF_8)
        val didExchangeInvitation = service.ariesAgent?.didExchangeController?.createInvitation(didExchangeData)
        if (didExchangeInvitation != null) {
            if (didExchangeInvitation.error != null) {
                println(didExchangeInvitation.error)
            } else {
                val actionsResponse = String(didExchangeInvitation.payload, StandardCharsets.UTF_8)
                println(actionsResponse)
            }

        }

        val oobController = service.ariesAgent?.outOfBandV2Controller
        //val payload = """ {"label":"${service.agentlabel}","goal":"$goal","goal_code":"$goalCode","protocols":["$protocols"], "router_connection_id":"${service.routerConnectionId}"} """
        //val data = payload.toByteArray(StandardCharsets.UTF_8)

        //val jsonInvitation = JSONObject(didExchangeInvitation?.let { String(it.payload, StandardCharsets.UTF_8) })
        val encodedString: String = Base64.getUrlEncoder().encodeToString(didExchangeInvitation?.payload)
        println(encodedString)


        val vdrController = service.ariesAgent?.vdrController
        val vdrRequest = """{"method": "peer", "opts": {"router_connection_id":"${service.routerConnectionId}"}}"""
        val vdrData = vdrRequest.toByteArray(StandardCharsets.UTF_8)
        val vdrResponse = vdrController?.createDID(vdrData)
        if (vdrResponse != null) {
            if (vdrResponse.error != null) {
                println(vdrResponse.error)
            } else {
                val actionsResponse = String(vdrResponse.payload, StandardCharsets.UTF_8)
                println(actionsResponse)
            }

        }

        val payloadV2 =
            """{"label":"${service.agentlabel}","body":{"goal":"$goal","goal_code":"$goalCode","accept":["didcomm/v2"]}, "from": "did:key:test", "attachments": [{"data" : "${encodedString}"}] }"""
        val dataTest = payloadV2.toByteArray(StandardCharsets.UTF_8)

        val invitation = oobController?.createInvitation(dataTest)
        if (invitation != null) {
            if (invitation.error != null) {
                println(invitation.error)
            } else {
                val actionsResponse = String(invitation.payload, StandardCharsets.UTF_8)
                println(actionsResponse)
            }

        }
    }
}