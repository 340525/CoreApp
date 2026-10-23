package com.t.core
import android.content.pm.ServiceInfo
import android.app.*
import android.content.*
import android.os.*
object VC {
    fun startFg(c: Context, i: Intent) { if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i) else c.startService(i) }
    fun fgType(svc: Service, id: Int, n: Notification) {
        if (Build.VERSION.SDK_INT >= 29) svc.startForeground(id, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        else svc.startForeground(id, n)
    }
    fun notifChannel(c: Context, id: String, name: String) {
        if (Build.VERSION.SDK_INT >= 26) c.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(id, name, NotificationManager.IMPORTANCE_MIN).apply { setShowBadge(false) })
    }
}
