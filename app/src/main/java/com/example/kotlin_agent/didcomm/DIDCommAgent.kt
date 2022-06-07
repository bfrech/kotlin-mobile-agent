package com.example.kotlin_agent.didcomm

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent
import java.util.*


class DIDCommAgent {

    var currentOpenDID = ""

    companion object {
        private var INSTANCE: DIDCommAgent? = null

        fun getInstance(): DIDCommAgent? {
            if (INSTANCE == null) {
                INSTANCE = DIDCommAgent()
            }
            return INSTANCE
        }
    }

    //private val peerDIDCreator = PeerDIDCreator()
    private val peerDIDDocResolver = PeerDIDDocResolver()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPeerDID(): String? {
        val ariesDID = AriesAgent.getInstance()?.createMyDID()
        val ariesDIDDoc = ariesDID?.let { AriesAgent.getInstance()?.vdrResolveDID(it) }

        //  DID needs to be stored until other DID is received from other Agent
        if (ariesDID != null) {
            currentOpenDID = ariesDID
        }

        val invitation = AriesAgent.getInstance()?.createDidExchangeInvitation()

        // Should return DIDDocEncoded
        //return encodeBase64(ariesDIDDoc)
        return invitation
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptPeerDIDInvitation(theirDIDDocEncoded: String, name: String): String? {

        val myAriesDID = AriesAgent.getInstance()?.createMyDID()
        val myAriesDIDDoc = myAriesDID?.let { AriesAgent.getInstance()?.vdrResolveDID(it) }

        // Then their DID:
        val theirDIDDoc = decodeBase64(theirDIDDocEncoded)
        println("Their DID Doc: $theirDIDDoc")
        val theirAriesDID = AriesAgent.getInstance()?.createTheirDID(theirDIDDoc)

        // Create new Connection with MyDID and TheirDID: can be a function
        if (theirAriesDID != null && myAriesDID != null) {
            val connectionID = AriesAgent.getInstance()?.createNewConnection(myAriesDID, theirAriesDID)

            // TODO: Save Connection ID and Name in Store
            println("Now Store connection with id: $connectionID and name: $name")
            if (connectionID != null) {

                println(AriesAgent.getInstance()?.getConnection(connectionID))

                // Message should include the decoded DIDDoc (or send from somehere else
                AriesAgent.getInstance()?.sendMessage("invitation-response", connectionID)
            }
        }

        println("Created Aries Connection with myDID: $myAriesDID and theirDID: $theirAriesDID")
        return encodeBase64(myAriesDIDDoc)
    }

    fun acceptDidExchangeInvitation(invitation: String){

    }


    /*
        After Other mobile Agent accepted Invitation, the other Agents' DID is returned and
        the connection is stored
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun completePeerDIDInvitation(theirDID: String, name: String){

        // First store theirDID in VDR
        val theirDIDDoc = peerDIDDocResolver.resolveToString(theirDID)
        val theirAriesDID = theirDIDDoc?.let { AriesAgent.getInstance()?.createDIDInVdr(it) }

        // Then store connection with myAriesDID and TheirAriesDID
        if (theirAriesDID != null && currentOpenDID != "") {
            val connectionID = AriesAgent.getInstance()?.createNewConnection(currentOpenDID, theirAriesDID)
            // DID is not open any longer
            currentOpenDID = ""

            // TODO: Save Connection ID and Name in Store
            println("Now Store connection with id: $connectionID and name: $name")
        }

        println("Created Aries Connection with myDID: $currentOpenDID and theirDID: $theirAriesDID")

        // TODO: Call AgentService to broadcast completed message

    }


    /*
        HELPER
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeBase64(encodeMe: String?): String {
        val payload = encodeMe?.toByteArray()
        val encodedBytes = Base64.getEncoder().encode(payload)
        return String(encodedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decodeBase64(encodedData: String): String {
        val decodedBytes = Base64.getDecoder().decode(encodedData.toByteArray())
        return String(decodedBytes)
    }


}