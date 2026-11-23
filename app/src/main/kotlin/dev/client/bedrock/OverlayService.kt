package dev.client.bedrock

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.IBinder
import android.view.*
import android.widget.*

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var button: TextView? = null
    private var panel: LinearLayout? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("overlay", "Overlay", NotificationManager.IMPORTANCE_LOW))
        startForeground(1, Notification.Builder(this, "overlay").setContentTitle("Bedrock Client running")
            .setSmallIcon(android.R.drawable.ic_menu_manage).build())
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addButton()
    }

    private fun params(w: Int = WindowManager.LayoutParams.WRAP_CONTENT) = WindowManager.LayoutParams(
        w, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 20; y = 200 }

    private fun addButton() {
        val lp = params()
        val btn = TextView(this).apply {
            text = "C"; textSize = 18f; setTextColor(Color.WHITE); setBackgroundColor(0xAA222222.toInt())
            setPadding(30, 20, 30, 20)
        }
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false; var downAt = 0L
        btn.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = lp.x; oy = lp.y; moved = false; downAt = System.currentTimeMillis() }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - sx).toInt(); val dy = (e.rawY - sy).toInt()
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > 10) moved = true
                    lp.x = ox + dx; lp.y = oy + dy; wm.updateViewLayout(v, lp)
                }
                MotionEvent.ACTION_UP -> if (!moved) {
                    if (System.currentTimeMillis() - downAt > 700) stopSelf() else togglePanel(lp.x, lp.y)
                }
            }
            true
        }
        button = btn; wm.addView(btn, lp)
    }

    private fun togglePanel(x: Int, y: Int) {
        panel?.let { wm.removeView(it); panel = null; return }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(0xDD111111.toInt()); setPadding(24, 16, 24, 16)
        }
        Modules.all.forEach { m ->
            box.addView(Switch(this).apply {
                text = m.name; setTextColor(Color.WHITE)
                isChecked = Modules.isOn(this@OverlayService, m.id)
                setOnCheckedChangeListener { _, on -> Modules.set(this@OverlayService, m.id, on) }
            })
        }
        box.addView(TextView(this).apply {
            text = "Close overlay"; setTextColor(Color.RED); textSize = 16f; setPadding(0, 24, 0, 8)
            setOnClickListener { stopSelf() }
        })
        // panel must be touchable for the close button
        val lp = params().apply { this.x = x + 120; this.y = y }
        panel = box; wm.addView(box, lp)
    }

    override fun onDestroy() {
        try { panel?.let { wm.removeView(it) } } catch (_: Throwable) {}
        try { button?.let { wm.removeView(it) } } catch (_: Throwable) {}
        panel = null; button = null
        super.onDestroy()
    }
}
