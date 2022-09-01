package com.example.kotlin_agent.store

import android.content.Context
import android.util.Log
import com.example.kotlin_agent.BuildConfig
import com.example.kotlin_agent.activities.messages.UserMessage

class AndroidFileSystemUtils {



    companion object {

        private val TAG = "AndroidFileSystemUtils"


        // Store MyDID -> ConnectionID for identification after message received
        fun storeConnectionIDForMyDID(context: Context, connectionID: String, myDID: String, oldDID: String = ""){

            Log.d(TAG, "Storing $myDID -> $connectionID to shared preferences")

            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_oldDIDs",
                Context.MODE_PRIVATE
            )

            with (sharedPrefs.edit()) {
                remove(oldDID)
                putString(myDID, connectionID)
                apply()
            }


        }


        // Get connectionID that belongs to MyDID
        fun getConnectionIDForMyDID(context: Context, did: String): String? {
            Log.d(TAG, "Fetching connectionID for $did from shared preferences")

            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_oldDIDs",
                Context.MODE_PRIVATE
            )
            return sharedPrefs.getString(did, "")
        }


        // Add theirLabel -> connectionID
        fun addLabelToSharedPrefs(context: Context, connectionID: String, label: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsLabel",
                Context.MODE_PRIVATE
            )
            sharedPrefs.edit().putString(label, connectionID).apply()
            Log.d(TAG, "Added $label -> $connectionID to shared preferences")
        }


        // Get Connection ID via Label
        fun getConnectionIDForLabel(context: Context, label: String): String? {
            Log.d(TAG, "Fetching connectionID for $label from shared preferences")
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsLabel",
                Context.MODE_PRIVATE
            )

            return sharedPrefs.getString(label, "")
        }

        // Get All names in contact list
        fun getContactList(context: Context): MutableSet<String> {
            Log.d(TAG, "Fetching all contacts from shared preferences")
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
            Log.d(TAG, "Added $connectionID -> $label to shared preferences")
        }

        fun getLabelForConnectionID(context: Context, connectionID: String): String? {
            Log.d(TAG, "Fetching label for $connectionID from shared preferences")
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesContactsID",
                Context.MODE_PRIVATE
            )
            return sharedPrefs.getString(connectionID, "")
        }






        // Store messages for Label
        fun storeMessageToSharedPrefs(context: Context, message: String, sent: Boolean, sharedPrefLabel: String, createdAt: String){
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$sharedPrefLabel",
                Context.MODE_PRIVATE
            )


            val label = if ( sent ) {
                "sent"
            } else {
                sharedPrefLabel
            }

            with (sharedPrefs.edit()) {
                putStringSet(createdAt, setOf("0000$label", "1111$message"))
                apply()
            }
        }


        fun getMessagesFromSharedPrefs(context: Context, sharedPrefLabel: String): MutableList<UserMessage> {
            val messageList = mutableListOf<UserMessage>()
            val sharedPrefs = context.getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$sharedPrefLabel",
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