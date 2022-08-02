package com.example.kotlin_agent.ariesAgent

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.kotlin_agent.BuildConfig
import com.example.kotlin_agent.Utils
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options


class AriesAgent(private val context: Context) {

    private val sharedPrefContacts: SharedPreferences by lazy {
        context.getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_sharedPreferences",
            Context.MODE_PRIVATE
        )
    }

    var ariesAgent: AriesController? = null
    var agentlabel: String = ""
    var routerConnectionId = ""
    var mediatorURL = ""

    var mediator: Mediator = Mediator(this)
    var connection: Connection = Connection(this)
    var messaging: Messaging = Messaging(this)
    var didHandler: DidHandler = DidHandler(this)
    var keyHandler: KeyHandler = KeyHandler(this)

    var openDID = ""


    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"
        //opts.storage = MyStorageProvider()

        try {

            println("Starting Agent with: $agentlabel")
            ariesAgent = Ariesagent.new_(opts)
            val handler = NotificationHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered didExchange handler with registration id: $registrationID")

            val messagingRegistrationID = ariesAgent?.registerHandler(handler, "basicmessage")
            println("registered didExchange handler with registration id: $messagingRegistrationID")

            val connectionRegID = ariesAgent?.registerHandler(handler, "connection_request")
            println("registered connection handler with registration id: $connectionRegID")

            val connectionResID = ariesAgent?.registerHandler(handler, "connection_response")
            println("registered connection handler with registration id: $connectionResID")

            val connectionCompleteID = ariesAgent?.registerHandler(handler, "connection_complete")
            println("registered connection handler with registration id: $connectionCompleteID")

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    /*
        Establish Connection
     */
    fun createConnectionInvitation(): String {
        return connection.createDIDExchangeInvitation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionRequest(invitation: String){

        val myDID = didHandler.createMyDID()
        //println("Created myDID: $myDID")
        openDID = myDID
        val myDIDDoc = didHandler.vdrResolveDID(myDID)
        //println("MyDIDDoc: $myDIDDoc")

        mediator.reconnectToMediator()
        messaging.sendMessageViaServiceEndpoint(Utils.encodeBase64(myDIDDoc), invitation, "connection_request")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionResponse(didDocEnc: String, label: String){
        val didDoc = Utils.decodeBase64(didDocEnc)
        println("Got Connection Request from $label with theirDID: $didDoc")

        val theirDID = didHandler.createTheirDIDFromDoc(didDoc)
        val myDID = didHandler.createMyDID()
        val connectionID = connection.createNewConnection(myDID, theirDID)
        println("Created Connection with: $connectionID")

        addContact(label, connectionID)

        val myDIDDoc = didHandler.vdrResolveDID(myDID)
        messaging.sendConnectionMessage(Utils.encodeBase64(myDIDDoc), connectionID, "connection_response")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completeConnectionRequest(didDocEnc: String, label: String){
        if(openDID == "") {
            println("No Open Connection Request!")
        }
        val theirDidDoc = Utils.decodeBase64(didDocEnc)
        val theirDID = didHandler.createTheirDIDFromDoc(theirDidDoc)
        val connectionID = connection.createNewConnection(openDID, theirDID)
        println("Created Connection with: $connectionID")

        addContact(label, connectionID)
        messaging.sendConnectionMessage("completed connection", connectionID, "connection_complete")
    }


    private fun addContact(label: String, connectionID: String){
        if(sharedPrefContacts.all.containsKey(label)){
            // TODO: Duplicate Handling
        }
        sharedPrefContacts.edit().putString(label, connectionID).apply()
        sendConnectionCompletedMessage(label)
    }

    private fun getConnectionIDFromLabel(label: String): String? {
        return if(sharedPrefContacts.all.containsKey(label)){
            sharedPrefContacts.getString(label, "")
        } else {
            ""
        }
    }




    /*
        Mediator Functions
     */
    fun connectToMediator(mediatorUrl: String){
        this.mediatorURL = mediatorUrl
        mediator.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediator.registerMediator()
    }



    /*
        Messaging
     */
    fun processBasicMessage(theirDID: String, message: String){
        println("TheirDID: $theirDID")

        // TODO: get connectionID and Label for TheirDID
        val connectionID = connection.getConnectionID(theirDID)
        val label = ""

        Utils.storeMessageToSharedPrefs(context, message, label)

        // notification of message: refresh messages page if open
        sendMessageReceivedMessage()

    }

    fun sendMessage(message: String, recipient: String){

        // TODO: rotate DIDs
        //rotateDIDForConnection(connectionID)

        val connectionID = getConnectionIDFromLabel(recipient)
        if (connectionID != null) {
            messaging.sendMessage(message, connectionID)
        }
    }



    /*
        Rotation
     */
    private fun rotateDIDForConnection(theirLabel: String){
        val connectionID = getConnectionIDFromLabel(theirLabel)

        // Only for testing, will be sent with next message normally
        if (connectionID != null) {
            val newDID =  connection.rotateDIDForConnection(connectionID)
            val newDIDDoc = didHandler.vdrResolveDID(newDID)
            println("New DID Doc: $newDIDDoc")
            messaging.sendMessage("Hey, I rotated my DID", connectionID)
        }
    }


    /*
         Communicate to Activity that connection was completed to go back to contacts screen
    */
    fun sendConnectionCompletedMessage(theirLabel: String){
        println("sender: Broadcasting message")
        val intent = Intent("connection_completed")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        //rotateDIDForConnection(theirLabel)
    }

    /*
        Communicate to MessagingActivity that a message was received
    */
    private fun sendMessageReceivedMessage(){
        val intent = Intent("received_new_message")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }




}