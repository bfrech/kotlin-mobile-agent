package com.example.kotlin_agent


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.activities.messages.UserMessage
import java.nio.charset.StandardCharsets
import java.util.*

class Utils {

   companion object {
       @RequiresApi(Build.VERSION_CODES.O)
       fun encodeBase64(str: String): String{
           return Base64.getEncoder().encodeToString(str.toByteArray())
       }


       @RequiresApi(Build.VERSION_CODES.O)
       fun decodeBase64(str: String): String {
           return String(Base64.getDecoder().decode(str.toByteArray()), StandardCharsets.UTF_8)
       }

       fun formatDateTime(timestamp: String): String{
           val time = timestamp.toLong()
           val sdf = java.text.SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
           return sdf.format(time)
       }



       fun storeMessageToSharedPrefs(context: Context, message: String, label: String){
           val sharedPrefs = context.getSharedPreferences(
               "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$label",
               Context.MODE_PRIVATE
           )
           val currentTimestamp = System.currentTimeMillis()

           with (sharedPrefs.edit()) {
               putStringSet(currentTimestamp.toString(), setOf("0000$label", "1111$message"))
               apply()
           }
       }


       fun getMessagesFromSharedPrefs(context: Context, label: String): MutableList<UserMessage> {
           val messageList = mutableListOf<UserMessage>()
           val sharedPrefs = context.getSharedPreferences(
               "${BuildConfig.APPLICATION_ID}_sharedPreferencesMessages_$label",
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