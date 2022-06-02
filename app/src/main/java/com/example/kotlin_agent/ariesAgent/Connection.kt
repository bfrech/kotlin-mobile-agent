package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.json.JSONObject
import java.nio.charset.StandardCharsets

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

    private fun createKeySet(keyType: String): String {
        //DIDComm V2 Key Agreement: NISTP384ECDHKW?
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
                return jsonObject.toString()
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
                println("ActionResponse: $actionsResponse")
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
        val routerConnection = getConnection(service.routerConnectionId)
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

        //val testService = """
        //    [  {
        //        "id": "",
        //        "type": "DIDCommMessaging",
        //        "serviceEndpoint": {
        //            "uri": "http://7bdb-84-58-54-76.eu.ngrok.io/invitation"
        //        },
        //        "routingKeys": ["did:key:daehdjkafdbkja"],
        //        "accept": ["didcomm/v2"]
        //    } ]
        //""".trimIndent()

        val kDid = createKeyDid()
        val jsonKeyDidDoc = JSONObject(kDid)

        val verificationMethod = jsonKeyDidDoc["verificationMethod"].toString()
        val keyAgreement = jsonKeyDidDoc["keyAgreement"].toString()

        val payload = """ {"@context":["https://w3id.org/did/v1"], "id": "#id" ,
            "service": $myService , 
            "verificationMethod":  $verificationMethod,
            "keyAgreement": $keyAgreement 
            } """
        return createDIDInVDR(payload)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createTheirDID(didDoc: String): String {
        val jsonDIDDoc = JSONObject(didDoc)
        val did = jsonDIDDoc["didDocument"].toString()
        return createDIDInVDR(did)
    }


    private fun createKeyDid(): String {
        val keyType = "ED25519"
        val key = createKeySet(keyType)
        println("Verification Key with Type: $keyType: $key")

        val payload ="""{"method":"key", "did": {"@context": ["https://www.w3.org/ns/did/v1"],
            "id": "",
            "verificationMethod":  [
            {
                "id": "",
                "type":"Ed25519VerificationKey2018",
                "controller":"",
                "publicKeyJwk": {        
                    "kty": "OKP",        
                    "crv": "Ed25519",        
                    "x": "$key"      
                }      
                }
            ]
            }}"""
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.createDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("ActionResponse: $actionsResponse")
                val jsonObject = JSONObject(actionsResponse)
                val did = jsonObject["did"].toString()
                return did
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    // Store Aries DID in VDR
    fun storeDIDInVDR(didDoc: String): String {

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