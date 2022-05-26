package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.api.Iterator
import org.hyperledger.aries.api.Store

class MyStorage(private val service: AriesAgent): Store {
    override fun batch(p0: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun delete(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun get(p0: String?): ByteArray {
        TODO("Not yet implemented")
    }

    override fun getBulk(p0: ByteArray?): ByteArray {
        TODO("Not yet implemented")
    }

    override fun getTags(p0: String?): ByteArray {
        TODO("Not yet implemented")
    }

    override fun put(p0: String?, p1: ByteArray?, p2: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun query(p0: String?, p1: Long): Iterator {
        TODO("Not yet implemented")
    }


}