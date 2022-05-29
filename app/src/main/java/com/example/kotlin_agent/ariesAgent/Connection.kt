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
            return "Res is null"
        }
        return ""
    }


    fun createNewConnection(myDID: String, theirDID: String): String {
        val request = """{"my_did": "$myDID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.createConnectionV2(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println(jsonObject["id"])
                return jsonObject["id"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    fun saveDIDInStore(did: String, name: String){
        val payload = """ {"did": {"@context":["https://w3id.org/did/v1"], ${did.drop(1).replace("\n","")}, "name":"$name"}""".trimMargin()
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.saveDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println(jsonObject)
            }
        } else {
            println("Res is null")
        }
    }


    fun getDID(did: String){
        val payload = """ {"id":"$did"}"""
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.getDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println(jsonObject)
            }
        } else {
            println("Res is null")
        }
    }

    fun vdrResolveDID(did: String): String {
        val payload = """ {"id":"$did"}"""
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.resolveDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println(jsonObject)
                return jsonObject.toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }

    fun createDIDInVDR(didDoc: String): String {
        val jsonObject = JSONObject(didDoc)
        val auth = jsonObject["authentication"]
        val payload = """ {"method":"peer", "did": {"@context":["https://w3id.org/did/v1"], ${didDoc.drop(1).dropLast(1).replace("\n","")}, "verificationMethod": $auth }} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.createDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                val did = jsonObject["did"].toString()
                val jsonDID = JSONObject(did)
                return jsonDID["id"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    // Store Aries DID in VDR
    fun storeDIDInVDR(didDoc: String): String {

        //val jsonObject = JSONObject(didDoc)
        //val auth = jsonObject["authentication"]
        println(didDoc)

        val payload = """ {"method":"peer", "did": $didDoc} """

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.createDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                val did = jsonObject["did"].toString()
                val jsonDID = JSONObject(did)
                return jsonDID["id"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }



}