package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.*

class Connection(private val service: AriesAgent) {

    // Create Out of Band Invitation for other Mobile Agent
    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBV2InvitationForMobileAgent(goal: String, goalCode: String) {

        // Call Create Peer DID from didcomm Agent
        val peerDID = ""

        val oobController = service.ariesAgent?.outOfBandV2Controller
        val payload = """{"label":"${service.agentlabel},"body":{"goal":"$goal","goal_code":"$goalCode","accept":["didcomm/v2", "didcomm/aip2;env=rfc19"]}, "from": "$peerDID"}}"""
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
            }
        }
        return key
    }

    fun getConnection(connectionID: String): String{
        val request = """{"id": "$connectionID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.queryConnectionByID(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                val result = JSONObject(jsonObject["result"].toString())
                return result.toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    fun createNewConnection(myDID: String, theirDID: String){
        val request = """{"my_did": "$myDID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.createConnectionV2(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println(jsonObject)
            }
        } else {
            println("Res is null")
        }
    }


}