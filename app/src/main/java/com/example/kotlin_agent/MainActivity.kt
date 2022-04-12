package com.example.kotlin_agent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlin_agent.ariesAgent.AriesAgentService
import com.example.kotlin_agent.didcomm.DIDCommService

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}