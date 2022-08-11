package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class DidHandler(private val service: AriesAgent) {

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
                return jsonObject["didDocument"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    fun saveDID(did: String, name: String): String {
        val payload = """ {"name":"$name", "did": $did }"""
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.saveDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("Saved DID: $actionsResponse")
            }
        } else {
            println("Res is null")
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDIDInVDR(did: String): String {
        val payload ="""{"method":"peer", "did": $did}"""
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

    /*
        Create Own DID in VDR (uses Router Connection
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createMyDID(): String {
        // Get the Router Connection to create Service Endpoint
        val routerConnection = service.connection.getConnection(service.routerConnectionId)
        val jsonRouterConnection = JSONObject(routerConnection)
        val serviceEndpointObject = jsonRouterConnection["ServiceEndPoint"].toString()
        val serviceEndpointJson = JSONObject(serviceEndpointObject)
        val serviceEndpointURI = serviceEndpointJson["uri"].toString()
        val serviceRoutingKeys = jsonRouterConnection["RecipientKeys"].toString()

        // Create Service:
        val myService = """
            [  {
                "id": "",
                "type": "DIDCommMessaging",
                "serviceEndpoint": {
                    "uri": "$serviceEndpointURI"
                },
                "routingKeys": $serviceRoutingKeys,
                "accept": ["didcomm/v2"]
            } ]
        """.trimIndent()

        // TODO: new way without did:key
        val verificationMethod = service.keyHandler.createVerificationMethod("ED25519", "Ed25519VerificationKey2018")
        val keyAgreement = service.keyHandler.createVerificationMethod("X25519ECDHKW", "X25519KeyAgreementKey2019")

        val payload = """ {"@context":["https://www.w3.org/ns/did/v1"], "id": "#id" ,
            "service": $myService ,
            "verificationMethod":  [$verificationMethod],
            "keyAgreement": [$keyAgreement] 
            } """

        return createDIDInVDR(payload)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createTheirDIDFromDoc(didDoc: String, label: String): String {
        saveDID(didDoc, label)
        val jsonDIDDoc = JSONObject(didDoc)
        val id = jsonDIDDoc["id"].toString()

        // TODO: Test
        println(vdrResolveDID(id))
        return id
    }

}