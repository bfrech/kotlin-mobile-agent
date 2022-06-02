package com.example.kotlin_agent.ariesAgent

import junit.framework.TestCase

class ConnectionTest : TestCase() {

    public override fun setUp() {
        super.setUp()
        AriesAgent.getInstance()?.createNewAgent("Alice")
    }

    public override fun tearDown() {}

    fun testCreateMyDID() {
        println(AriesAgent.getInstance()?.createMyDID())
    }
}