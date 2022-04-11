package com.example.kotlin_agent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlin_agent.ariesAgent.AriesAgentService
import com.example.kotlin_agent.didcomm.DIDCommService

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {

        // Create Aries Agent and DIDComm Service: Should happen after user typed in name + mediator
        val ariesAgentService = AriesAgentService()
        ariesAgentService.createNewAgent("Mobile Agent A")
        ariesAgentService.connectToMediator("http://0e7c-84-58-54-76.eu.ngrok.io/invitation")
        val didCommService = DIDCommService(ariesAgentService)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}