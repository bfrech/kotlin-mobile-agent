package com.example.kotlin_agent.ariesAgent

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.kotlin_agent.Utils
import com.example.kotlin_agent.store.AndroidFileSystemUtils
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options


class AriesAgent(private val context: Context) {

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
            registerNotificationHandlers()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun registerNotificationHandlers() {
        val handler = NotificationHandler(this)
        val registrationID = this.ariesAgent?.registerHandler(handler, "didexchange_states")
        println("registered did exchange handler with registration id: $registrationID")

        val messagingRegistrationID = this.ariesAgent?.registerHandler(handler, "mobile_message")
        println("registered mobile message handler with registration id: $messagingRegistrationID")

        val connectionRegID = this.ariesAgent?.registerHandler(handler, "connection_request")
        println("registered connection request handler with registration id: $connectionRegID")

        val connectionResID = this.ariesAgent?.registerHandler(handler, "connection_response")
        println("registered connection response handler with registration id: $connectionResID")

        val connectionCompleteID = this.ariesAgent?.registerHandler(handler, "connection_complete")
        println("registered connection complete handler with registration id: $connectionCompleteID")
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

        val label = AriesUtils.extractValueFromJSONObject(
            connectionHandler.getConnection(connectionID),
            AriesUtils.THEIR_LABEL_KEY
        )
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

        AndroidFileSystemUtils.addLabelToSharedPrefs(context, connectionID, label)
        AndroidFileSystemUtils.addConnectionIDToSharedPrefs(context, connectionID, label)

        val myDID = connectionHandler.getMyDIDForConnection(connectionID)
        AndroidFileSystemUtils.storeConnectionIDForMyDID(context,connectionID, myDID)

        sendConnectionCompletedBroadcast()
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
    fun processBasicMessage(theirDID: String, myDID: String, message: String, createdAt: String){

        val connectionID = AndroidFileSystemUtils.getConnectionIDForMyDID(context, myDID)
        println(connectionHandler.getConnection(connectionID!!))

        if(connectionID == ""){
            println("No connection Entry for This Label")
            return
        } else {
            val theirOldDID = connectionHandler.getTheirDIDForConnection(connectionID)
            if(theirOldDID != theirDID){
                println("They rotated DIDs, Updating Connection Entry with new connectionID: $connectionID!")
                connectionHandler.updateTheirDIDForConnection(connectionID, theirDID)

                val label = AndroidFileSystemUtils.getLabelForConnectionID(context, connectionID)

                if (label != null) {
                    AndroidFileSystemUtils.storeMessageToSharedPrefs(context, message, false, label, createdAt )
                }
            }
        }

        // TODO: notification of message: refresh messages page if open
        sendMessageReceivedMessage()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(message: String, connectionID: String){
        rotateDIDForConnection(connectionID)
        messagingHandler.sendMobileMessage(message, connectionID)
    }



    /*
        Rotation
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun rotateDIDForConnection(connectionID: String){
        val newDID =  connectionHandler.rotateDIDForConnection(connectionID)
        AndroidFileSystemUtils.storeConnectionIDForMyDID(context, connectionID, newDID)
    }




    /*
         Communicate to Activity that connection was completed to go back to contacts screen
    */
    private fun sendConnectionCompletedBroadcast(){
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