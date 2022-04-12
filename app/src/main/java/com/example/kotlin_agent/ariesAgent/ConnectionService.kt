package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

class ConnectionService(private val service: AriesAgentService) {

    // Create Out of Band Invitation for other Mobile Agent
    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBV2InvitationForMobileAgent(goal: String, goalCode: String) {

        val didExchangeRequest = createDIDExchangeRequest()
        val encodedString: String = Base64.getUrlEncoder()
            .encodeToString(didExchangeRequest.toByteArray(StandardCharsets.UTF_8))

        val oobController = service.ariesAgent?.outOfBandV2Controller
        val payload =
            """{"label":"${service.agentlabel}","body":{"goal":"$goal","goal_code":"$goalCode",
                |"accept":["didcomm/v2"]}, "from": "did:key:test", 
                |"attachments": [{"@id": "request-0", "mime-type": "application/json", "data": {"base64": "${encodedString}"}}] }""".trimMargin()
        val dataTest = payload.toByteArray(StandardCharsets.UTF_8)

        val invitation = oobController?.createInvitation(dataTest)
        if (invitation != null) {
            if (invitation.error != null) {
                println(invitation.error)
            } else {
                val actionsResponse = String(invitation.payload, StandardCharsets.UTF_8)
                println(actionsResponse)
                // TODO: generate QR Code from invitation instead of sending

            }
        }
    }

    // Accept Out of band V2 invitation after scanning QR Code
    fun acceptOOBV2Invitation(inv: String){

        val invitation = """ ${inv.dropLast(1)}, "my_label": "${service.agentlabel}" }"""

        var res: ResponseEnvelope
        try {
            val outOfBandV2Controller = service.ariesAgent?.outOfBandV2Controller
            val data = invitation.toByteArray(StandardCharsets.UTF_8)
            if (outOfBandV2Controller != null) {
                res = outOfBandV2Controller.acceptInvitation(RequestEnvelope(data))
                if(res.error != null){
                    println(res.error.message)
                } else {
                    val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                    val jsonActionResponse = JSONObject(actionsResponse)
                    val connectionID = jsonActionResponse["connection_id"].toString()
                    println("""Accepted Invitation of Mobile Agent with: ${connectionID}""")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper Function: only for testing purpose
    //fun fetchInvitation(agentUrl: String): String{
    //    val url = URL(agentUrl)
    //    val connection = url.openConnection()
    //    val content = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
    //    return """{ $content, "my_label": "${service.agentlabel}" }"""
    //}


    private fun createKeySet(keyType: String): String {
        //DIDComm V2 Key Agreement: NISTP384ECDHKW
        val kmsController = service.ariesAgent?.kmsController
        val kmsRequest = """{"keyType":"$keyType"}"""
        val kmsData = kmsRequest.toByteArray(StandardCharsets.UTF_8)
        val kmsResponse = kmsController?.createKeySet(RequestEnvelope(kmsData))

        var key = ""
        if (kmsResponse != null) {
            if (kmsResponse.error != null) {
                println(kmsResponse.error)
            } else {
                val actionsResponse = String(kmsResponse.payload, StandardCharsets.UTF_8)
                val jsonActionResponse = JSONObject(actionsResponse)
                key = jsonActionResponse["publicKey"].toString()
                println(key)
            }
        }
        return key
    }

    private fun createPeerDID(key: String) {
        val vdrController = service.ariesAgent?.vdrController
        val vdrRequest =
            """{"method": "peer", "opts": {"router_connection_id":"${service.routerConnectionId}"}, "did": {"@context":["https://w3id.org/did/v1","https://w3id.org/did/v2"], "id": "id", "keyAgreement": []}}"""
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
    }


    private fun createDIDExchangeRequest(): String {
        val didExchangeRequest =
            """{"alias": "${service.agentlabel}", "router_connection_id":"${service.routerConnectionId}"}"""
        val didExchangeData = didExchangeRequest.toByteArray(StandardCharsets.UTF_8)
        val didExchangeInvitation =
            service.ariesAgent?.didExchangeController?.createInvitation(didExchangeData)
        if (didExchangeInvitation != null) {
            if (didExchangeInvitation.error != null) {
                println(didExchangeInvitation.error)
                return ""
            }
            val actionsResponse = String(didExchangeInvitation.payload, StandardCharsets.UTF_8)
            return actionsResponse
        }
        return ""
    }


}