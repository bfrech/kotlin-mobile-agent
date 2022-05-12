package com.example.kotlin_agent.didcomm


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

    fun createPeerDID(){
        peerDIDCreator.createPeerDID()
    }
}