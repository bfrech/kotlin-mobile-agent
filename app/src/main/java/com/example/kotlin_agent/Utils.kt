package com.example.kotlin_agent



import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

       @RequiresApi(Build.VERSION_CODES.O)
       fun formatDateTime(isoDateString: String): String{
           val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS +0000 'UTC'")
           //var date = OffsetDateTime.parse(isoDateString.replace("Z$", "+0000"))
           var date = LocalDateTime.parse(isoDateString, formatter)
           var converter = DateTimeFormatter.ofPattern("HH:mm:ss")
           return converter.format(date)
       }

       @RequiresApi(Build.VERSION_CODES.O)
       fun getCurrentTimeAsIsoString(): String {
           val now = LocalDateTime.now()
           val offsetDate = OffsetDateTime.of(now, ZoneOffset.UTC)
           return offsetDate.format(DateTimeFormatter.ISO_DATE_TIME)
       }

       @RequiresApi(Build.VERSION_CODES.O)
       fun getCurrentTimeAsFormattedString(): String {
           val now = LocalDateTime.now()
           val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS +0000 'UTC'")
           return formatter.format(now)
       }

       fun createUniqueID(): String {
           return UUID.randomUUID().toString()
       }



   }


}