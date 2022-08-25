package com.example.kotlin_agent



import android.os.Build
import androidx.annotation.RequiresApi
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



   }


}