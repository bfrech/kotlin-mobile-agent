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
import org.json.JSONObject


class AriesAgent(private val context: Context) {

    private val sharedPrefContacts: SharedPreferences by lazy {
        context.getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_sharedPreferences",
            Context.MODE_PRIVATE
        )
    }

    private val sharedPrefLabels: SharedPreferences by lazy {
        context.getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_sharedPreferencesLabels",
            Context.MODE_PRIVATE
        )
    }

    var ariesAgent: AriesController? = null
    var agentlabel: String = ""
    var routerConnectionId = ""
    var mediatorURL = ""

    var mediatorHandler: MediatorHandler = MediatorHandler(this)
    var connectionHandler: ConnectionHandler = ConnectionHandler(this)
    var messagingHandler: MessagingHandler = MessagingHandler(this)
    var didHandler: DidHandler = DidHandler(this)
    var keyHandler: KeyHandler = KeyHandler(this)

    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"

        opts.logLevel = "debug"
        //opts.storage = MyStorageProvider()

        try {

            println("Starting Agent with: $agentlabel")
            ariesAgent = Ariesagent.new_(opts)
            val handler = NotificationHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered didExchange handler with registration id: $registrationID")

            val messagingRegistrationID = ariesAgent?.registerHandler(handler, "mobile_message")
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
        Connection Messages to establish a connection with other mobile agents
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createConnectionInvitation(): String {
        return connectionHandler.createDIDExchangeInvitation()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionRequest(invitation: String){
        val oobInvitation = connectionHandler.createOOBInvitation()
        mediatorHandler.reconnectToMediator()
        messagingHandler.sendMessageViaServiceEndpoint("connection_request", Utils.encodeBase64(oobInvitation), invitation)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionResponse(invitationEnc: String){
        val invitation = Utils.decodeBase64(invitationEnc)
        println("Got Connection Request with invitation: $invitation")

        val connectionID = connectionHandler.acceptOOBV2Invitation(invitation)
        println("Accepted OOB Invitation with $connectionID")

        // TODO: get label from connection and store in shared Prefs
        val jsonConnection = JSONObject(connectionHandler.getConnection(connectionID))
        val label = jsonConnection["TheirLabel"].toString()
        addContact(label, connectionID)

        messagingHandler.sendConnectionResponse(connectionID, "connection_response")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completeConnectionRequest(label: String, from: String, to: String){
        val connectionID = connectionHandler.createNewConnection(to, from)
        println("Created Connection with $label and: $connectionID")
        addContact(label, connectionID)
    }

    private fun addContact(label: String, connectionID: String){
        if(sharedPrefContacts.all.containsKey(connectionID)){
            // TODO: Duplicate Handling
        }
        sharedPrefContacts.edit().putString(label, connectionID).apply()

        // TODO: Only use connectionID
        sharedPrefLabels.edit().putString(connectionID, label).apply()

        // TODO: store my DID -> connID
        val myDID = connectionHandler.getMyDIDForConnection(connectionID)
        Utils.storeConnectionIDForOldDID(context,connectionID, myDID)

        println("new Connection: ${connectionHandler.getConnection(connectionID)}")

        sendConnectionCompletedBroadcast()
    }

    private fun getConnectionIDFromLabel(label: String): String {
        return if(sharedPrefContacts.all.containsKey(label)){
            sharedPrefContacts.getString(label, "").toString()
        } else {
            ""
        }
    }


    /*
        Mediator Functions
     */
    fun connectToMediator(mediatorUrl: String){
        this.mediatorURL = mediatorUrl
        mediatorHandler.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediatorHandler.registerMediator()
    }


    /*
        Messaging
     */
    fun processBasicMessage(theirDID: String, myDID: String, message: String){

        val connectionID = Utils.getConnectionIDForMyOldDID(context, myDID)
        println(connectionHandler.getConnection(connectionID!!))

        if(connectionID == ""){
            println("No connection Entry for This Label")
            return
        } else {
            val theirOldDID = connectionHandler.getTheirDIDForConnection(connectionID)
            if(theirOldDID != theirDID){
                println("They rotated DIDs, Updating Connection Entry with new connectionID: $connectionID!")
                connectionHandler.updateTheirDIDForConnection(connectionID, theirDID)

                // TODO: get label for connection
                val label = sharedPrefLabels.getString(connectionID, "")

                if (label != null) {
                    Utils.storeMessageToSharedPrefs(context, message, false, label)
                }
            }
        }

        // TODO: notification of message: refresh messages page if open
        sendMessageReceivedMessage()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(message: String, recipient: String){
        val connectionID = getConnectionIDFromLabel(recipient)
        if (connectionID != "") {
            rotateDIDForConnection(connectionID)
            messagingHandler.sendMobileMessage(message, connectionID)
        }
    }



    /*
        Rotation
     */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun rotateDIDForConnection(connectionID: String){
        val newDID =  connectionHandler.rotateDIDForConnection(connectionID)
        Utils.storeConnectionIDForOldDID(context, connectionID, newDID)
    }


    /*
         Communicate to Activity that connection was completed to go back to contacts screen
    */
    private fun sendConnectionCompletedBroadcast(){
        println("sender: Broadcasting message")
        val intent = Intent("connection_completed")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    /*
        Communicate to MessagingActivity that a message was received
    */
    private fun sendMessageReceivedMessage(){
        val intent = Intent("received_new_message")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }




}