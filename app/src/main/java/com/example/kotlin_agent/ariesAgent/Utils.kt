package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.util.Base64

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
   }


}