package com.example.kotlin_agent.store

import org.hyperledger.aries.api.Provider
import org.hyperledger.aries.api.Store


class MyStorageProvider: Provider {
    override fun close() {
        TODO("Not yet implemented")
    }

    override fun getStoreConfig(p0: String?): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openStore(p0: String?): Store {
        TODO("Not yet implemented")
    }

    override fun setStoreConfig(p0: String?, p1: ByteArray?) {
        TODO("Not yet implemented")
    }
}