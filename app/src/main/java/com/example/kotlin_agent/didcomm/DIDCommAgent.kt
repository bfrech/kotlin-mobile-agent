package com.example.kotlin_agent.didcomm

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.ariesAgent.AriesAgent


class DIDCommAgent {

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

    fun createPeerDID(){
        peerDIDCreator.createPeerDID()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun acceptPeerDIDInvitation(theirDID: String, name: String) {
        val myDid = peerDIDCreator.createPeerDID()
        val theirDIDDoc = peerDIDDocResolver.resolveToString(theirDID)

        // Save DID in Store
        //AriesAgent.getInstance()?.saveDID(theirDIDDoc, name)

        // Create DID in VDR
        val peerDID = AriesAgent.getInstance()?.createDIDInVdr(theirDIDDoc)


        if (peerDID != null) {
            AriesAgent.getInstance()?.vdrResolveDID(peerDID)
        }

        // TODO: create new Connection with MyDID and TheirDID
        if (peerDID != null) {
            AriesAgent.getInstance()?.createNewConnection(myDid, peerDID)
        }
    }
}