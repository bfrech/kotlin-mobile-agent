package com.example.kotlin_agent

import com.example.kotlin_agent.ariesAgent.AriesAgentService
import junit.framework.TestCase

class AriesAgentServiceTest : TestCase() {

    var service: AriesAgentService? = null

    public override fun setUp() {
        super.setUp()
        service = AriesAgentService()
    }

    public override fun tearDown() {}

    fun testCreateNewAgent() {
        service?.createNewAgent("Test Agent A")
    }

    fun testConnectToMediator() {
        service?.createNewAgent("Test Agent A")
        service?.connectToMediator("http://a3ec-84-58-54-76.eu.ngrok.io/invitation")
        Thread.sleep(5000)
        service?.createOOBV2InvitationForMobileAgent()
        Thread.sleep(10000)
    }
}