package com.example.kotlin_agent.store

import android.content.Context
import com.example.kotlin_agent.BuildConfig
import com.example.kotlin_agent.activities.messages.UserMessage

class AndroidFileSystemUtils {

    companion object {

        // Store MyDID -> ConnectionID for identification after message received
        fun storeConnectionIDForMyDID(context: Context, connectionID: String, myDID: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_oldDIDs",
                Context.MODE_PRIVATE
            )

            with (sharedPrefs.edit()) {
                putString(myDID, connectionID)
                apply()
            }

        }

        // Remove MyOldDID -> ConnectionID
        fun removeConnectionIDForMyDID(context: Context, myOldDID: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_oldDIDs",
                Context.MODE_PRIVATE
            )

            with (sharedPrefs.edit()) {
                remove(myOldDID)
                apply()
            }

        }

        // Get connectionID that belongs to MyDID
        fun getConnectionIDForMyDID(context: Context, did: String): String? {
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_oldDIDs",
                Context.MODE_PRIVATE
            )
            return sharedPrefs.getString(did, "")
        }


        // Add theirlabel -> connectionID
        fun addLabelToSharedPrefs(context: Context, connectionID: String, label: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsLabel",
                Context.MODE_PRIVATE
            )
            sharedPrefs.edit().putString(label, connectionID).apply()
            println("Added $label -> $connectionID to Contacts")
        }


        // Get Connection ID via Label
        fun getConnectionIDForLabel(context: Context, label: String): String? {
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsLabel",
                Context.MODE_PRIVATE
            )

            return sharedPrefs.getString(label, "")
        }

        // Get All names in contact list
        fun getContactList(context: Context): MutableSet<String> {
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsLabel",
                Context.MODE_PRIVATE
            )
            return sharedPrefs.all.keys
        }



        // Add connectionID -> Label to Shared Preferences
        fun addConnectionIDToSharedPrefs(context: Context, connectionID: String, label: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsID",
                Context.MODE_PRIVATE
            )
            sharedPrefs.edit().putString(connectionID, label).apply()
            println("Added $connectionID -> $label to Contacts")
        }

        fun getLabelForConnectionID(context: Context, connectionID: String): String? {
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsID",
                Context.MODE_PRIVATE
            )
            return sharedPrefs.getString(connectionID, "")
        }






        // Store messages for Label
        fun storeMessageToSharedPrefs(context: Context, message: String, sent: Boolean, sharedPreflabel: String, createdAt: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$sharedPreflabel",
                Context.MODE_PRIVATE
            )


            val label = if ( sent ) {
                "sent"
            } else {
                sharedPreflabel
            }

            with (sharedPrefs.edit()) {
                putStringSet(createdAt, setOf("0000$label", "1111$message"))
                apply()
            }
        }


        fun getMessagesFromSharedPrefs(context: Context, sharedPreflabel: String): MutableList<UserMessage> {
            val messageList = mutableListOf<UserMessage>()
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$sharedPreflabel",
                Context.MODE_PRIVATE
            )

            sharedPrefs.all.keys.forEach{ k ->
                val content = sharedPrefs.getStringSet(k, setOf("", ""))?.sorted()
                val message = content?.let { UserMessage(it.elementAt(1).drop(4), it.elementAt(0).drop(4), k) }
                if (message != null) {
                    messageList.add(message)
                }
            }
            return messageList
        }




    }
}