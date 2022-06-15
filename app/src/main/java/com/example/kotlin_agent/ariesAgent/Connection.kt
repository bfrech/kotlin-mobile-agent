package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.models.RequestEnvelope
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Connection(private val service: AriesAgent) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createServiceEndpointInvitation(): String {
        val did = createMyDID()
        val res = JSONObject(vdrResolveDID(did))

        println("My DID: $res")

        val didDoc = JSONObject(res["didDocument"].toString())
        val service = JSONObject(didDoc["service"].toString().drop(1).dropLast(1))
        val serviceEndpoint = JSONObject(service["serviceEndpoint"].toString())
        //println(serviceEndpoint)

        val payload = """{
            |"serviceEndpoint": "${serviceEndpoint["uri"]}",
            |"routingKeys": ${service["routingKeys"]},
            |"recipientKeys": ${service["recipientKeys"]}
            |}""".trimMargin()

        // extract service
        return payload
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptConnectionInvitation(invitation: String): String {
        val myDID = createMyDID()
        val theirDID = createTheirDID(invitation)

        // TEST
        val myDIDDoc = vdrResolveDID(myDID)
        val theirDIDDoc = vdrResolveDID(theirDID)
        println("My DID Doc: $myDIDDoc")
        println("Their DID Doc: $theirDIDDoc")

        return createNewConnection(myDID, theirDID)
    }

    private fun createKeySet(keyType: String): String {
        //DIDComm V2 Key Agreement: NISTP384ECDHKW or X25519ECDH
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
        //println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.vdrController?.createDID(data)
        if(res != null){
            if(res.error != null){
                println("Error: ${res.error}")
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                //println("ActionResponse: $actionsResponse")
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
        //println("Router: $routerConnection")
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

        val kDid = createKeyDid()

        println("My Key Did: $kDid")

        val jsonKeyDidDoc = JSONObject(kDid)
        val verificationMethod = jsonKeyDidDoc["verificationMethod"].toString()
        val keyAgreement = jsonKeyDidDoc["keyAgreement"].toString()

        val payload = """ {"@context":["https://www.w3.org/ns/did/v1"], "id": "#id" ,
            "service": $myService , 
            "verificationMethod":  $verificationMethod,
            "keyAgreement": $keyAgreement 
            } """
        return createDIDInVDR(payload)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createTheirDID(invitation: String): String {
        val jsonResponse = JSONObject(invitation)
        val invitation = jsonResponse["invitation"].toString()
        val jsonInvitation = JSONObject(invitation)

        val serviceEndpoint = jsonInvitation["serviceEndpoint"].toString()
        val recipientKeys = jsonInvitation["recipientKeys"].toString()
        val routingKeys = jsonInvitation["routingKeys"].toString()
        val alias = jsonResponse["alias"].toString()

        // Create Service:
        val theirService = """
            [  {
                "id": "",
                "type": "DIDCommMessaging",
                "serviceEndpoint": {
                    "uri": "$serviceEndpoint"
                },
                "routingKeys": $routingKeys,
                "accept": ["didcomm/v2"]
            } ]
        """.trimIndent()

        val kDid = createKeyDid()
        val jsonKeyDidDoc = JSONObject(kDid)
        val verificationMethod = jsonKeyDidDoc["verificationMethod"].toString()
        val keyAgreement = jsonKeyDidDoc["keyAgreement"].toString()

        val payload = """ {"@context":["https://www.w3.org/ns/did/v1"], "id": "$alias" ,
            "service": $theirService , 
            "verificationMethod":  $verificationMethod,
            "keyAgreement": $keyAgreement 
            } """

        return createDIDInVDR(payload)
    }


    private fun createKeyDid(): String {
        val keyType = "ED25519"
        val key = createKeySet(keyType)
        println("Verification Key with Type: $keyType: $key")

        val payload ="""{"method":"key", "did": {"@context": ["https://www.w3.org/ns/did/v1"],
            "id": "1234",
            "verificationMethod":  [
            {
                "id": "key-1‚",
                "type":"Ed25519VerificationKey2018",
                "controller":"",
                "publicKeyBase58": "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"      
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