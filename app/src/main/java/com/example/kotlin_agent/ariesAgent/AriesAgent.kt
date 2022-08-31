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
    lateinit var agentlabel: String
    lateinit var routerConnectionId: String
    private lateinit var mediatorURL: String

    private lateinit var openInvitationID: String

    var mediatorHandler: MediatorHandler = MediatorHandler(this)
    var connectionHandler: ConnectionHandler = ConnectionHandler(this)
    private var messagingHandler: MessagingHandler = MessagingHandler(this)
    var didHandler: DidHandler = DidHandler(this)
    var keyHandler: KeyHandler = KeyHandler(this)

   private val notificationHandler: NotificationHandler = NotificationHandler(this)

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
        val registrationID = this.ariesAgent?.registerHandler(notificationHandler, "didexchange_states")
        println("registered did exchange handler with registration id: $registrationID")

        val messagingRegistrationID = this.ariesAgent?.registerHandler(notificationHandler, "mobile_message")
        println("registered mobile message handler with registration id: $messagingRegistrationID")

        val connectionRegID = this.ariesAgent?.registerHandler(notificationHandler, "connection_request")
        println("registered connection request handler with registration id: $connectionRegID")

        val connectionResID = this.ariesAgent?.registerHandler(notificationHandler, "connection_response")
        println("registered connection response handler with registration id: $connectionResID")

        val connectionCompleteID = this.ariesAgent?.registerHandler(notificationHandler, "connection_complete")
        println("registered connection complete handler with registration id: $connectionCompleteID")
    }

    /*
        Connection Messages to establish a connection with other mobile agents
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createConnectionInvitation(): String {
        val invitation = connectionHandler.createDIDExchangeInvitation()
        openInvitationID = invitation.second
        return invitation.first
    }

    //private fun subscribeToConnectionInvitation(invitationID: String): String? {
    //    return this.ariesAgent?.registerHandler(notificationHandler, "connection_request_$invitationID")
    //}

    //private fun unsubscribeToConnectionInvitation(){
    //    println("Unregistering Handler with: $openInvitationID")
    //    this.ariesAgent?.unregisterHandler("connection_request_$openInvitationID")
    //    openInvitationID = ""
    //}


    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionRequest(invitation: String){
        val oobInvitation = connectionHandler.createOOBInvitation()
        val invitationID = AriesUtils.extractValueFromJSONObject(invitation, AriesUtils.INVITATION_ID_KEY)
        mediatorHandler.reconnectToMediator()
        messagingHandler.sendConnectionRequest("connection_request", Utils.encodeBase64(oobInvitation), invitation, invitationID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionResponse(message: String){

        val messageParts = message.split(";")
        val invitationID = messageParts[0]
        val invitationEnc = messageParts[1]

        if (invitationID != openInvitationID) {
            println("Invitation is not valid anymore!")
            return
        }

        val invitation = Utils.decodeBase64(invitationEnc)
        println("Got Connection Request with invitation: $invitation")

        val connectionID = connectionHandler.acceptOOBV2Invitation(invitation)
        println("Accepted OOB Invitation with $connectionID")

        val label = AriesUtils.extractValueFromJSONObject(
            connectionHandler.getConnection(connectionID),
            AriesUtils.THEIR_LABEL_KEY
        )
        addContact(label, connectionID)
        messagingHandler.sendConnectionResponse(connectionID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completeConnectionRequest(message: String, from: String){
        val messageParts = message.split(";")
        val label = messageParts[0]
        val myDID = messageParts[1]

        val connectionID = connectionHandler.createNewConnection(myDID, from)
        println("Created Connection with $label and: $connectionID")
        rotateDIDForConnection(connectionID)
        messagingHandler.sendConnectionMessage(connectionID, "connection_complete")
        addContact(label, connectionID)

    }

    fun acknowledgeConnectionComplete(label: String){
        println("Connection with $label acknowledged")
        openInvitationID = ""
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
        this.routerConnectionId = mediatorHandler.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediatorHandler.registerMediator()
    }


    /*
        Messaging
     */
    fun processMobileMessage(theirDID: String, myDID: String, message: String, createdAt: String){

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
        val oldDID = connectionHandler.getMyDIDForConnection(connectionID)
        val newDID =  connectionHandler.rotateDIDForConnection(connectionID)
        AndroidFileSystemUtils.storeConnectionIDForMyDID(context, connectionID, newDID, oldDID)
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