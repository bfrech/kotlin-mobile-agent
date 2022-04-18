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


    fun testConnectToMediator() {
        service?.setupAgentWithLabelAndMediator("http://f11e-84-58-54-76.eu.ngrok.io/invitation", "Alice")
        Thread.sleep(5000)
        service?.createOOBInvitationForMobileAgent()
        Thread.sleep(100000)
    }
}