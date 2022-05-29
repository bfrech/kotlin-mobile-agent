package com.example.kotlin_agent.didcomm

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent


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

    private val peerDIDCreator = PeerDIDCreator()
    private val peerDIDDocResolver = PeerDIDDocResolver()

    fun createPeerDID(): String {
        val did = peerDIDCreator.createPeerDID()
        val didDoc = peerDIDDocResolver.resolveToString(did)
        val ariesDID = AriesAgent.getInstance()?.createDIDInVdr(didDoc)

        // TODO: did should be returned to other Agent to circumvent resolver problem, but ariesDID
        //  needs to be stored until other DID is received from other Agent
        if (ariesDID != null) {
            currentOpenDID = ariesDID
        }
        return did
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun acceptPeerDIDInvitation(theirDID: String, name: String): String {

        // Create own peer DID (this should be returned to the other Agent) and store in VDR
        val myDid = peerDIDCreator.createPeerDID()
        val myDIDDoc =  peerDIDDocResolver.resolveToString(myDid)
        val myAriesDID = AriesAgent.getInstance()?.createDIDInVdr(myDIDDoc)

        // Then their DID:
        val theirDIDDoc = peerDIDDocResolver.resolveToString(theirDID)
        val theirAriesDID = theirDIDDoc?.let { AriesAgent.getInstance()?.createDIDInVdr(it) }

        // Create new Connection with MyDID and TheirDID: can be a function
        if (theirAriesDID != null && myAriesDID != null) {
            val connectionID = AriesAgent.getInstance()?.createNewConnection(myAriesDID, theirAriesDID)

            // TODO: Save Connection ID and Name in Store
            println("Now Store connection with id: $connectionID and name: $name")
        }

        println("Created Aries Connection with myDID: $myAriesDID and theirDID: $theirAriesDID")

        // Other user needs peerDID from lib
        return myDid
    }


    /*
        After Other mobile Agent accepted Invitation, the other Agents' DID is returned and
        the connection is stored
     */
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
}