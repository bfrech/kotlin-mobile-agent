package com.example.kotlin_agent

import com.example.kotlin_agent.ariesAgent.AriesAgent
import junit.framework.TestCase

class AgentServiceTest : TestCase() {

    var service: AriesAgent? = null

    public override fun setUp() {
        super.setUp()
        service = AriesAgent()
    }

    public override fun tearDown() {}


    fun testConnectToMediator() {
        service?.createNewAgent("Alice")
        service?.connectToMediator("http://ddef-84-58-54-76.eu.ngrok.io/invitation")
        Thread.sleep(5000)
        //service?.createDIDExchangeRequest()
        Thread.sleep(100000)
    }
}