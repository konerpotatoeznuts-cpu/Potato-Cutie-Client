package dev.client.bedrock

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class LauncherActivity : Activity() {
    private val bedrock = "com.mojang.minecraftpe"

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        try { build() } catch (t: Throwable) {
            setContentView(ScrollView(this).apply {
                addView(TextView(this@LauncherActivity).apply {
                    text = "Startup error (send me a screenshot):\n\n" + android.util.Log.getStackTraceString(t)
                    setTextIsSelectable(true); setPadding(30, 60, 30, 30)
                })
            })
        }
    }

    private fun build() {
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        root.addView(TextView(this).apply { text = "Bedrock Client"; textSize = 26f })
        root.addView(TextView(this).apply { text = "Pick modules, then launch the game. A floating button gives you the menu in-game." })
        Modules.all.forEach { m ->
            root.addView(Switch(this).apply {
                text = "${m.name} - ${m.desc}"
                isChecked = Modules.isOn(this@LauncherActivity, m.id)
                setOnCheckedChangeListener { _, on -> Modules.set(this@LauncherActivity, m.id, on) }
                setPadding(0, pad / 2, 0, pad / 2)
            })
        }
        root.addView(Button(this).apply { text = "Launch Minecraft"; setOnClickListener { launch() } })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun launch() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow 'display over other apps' first", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        val game = packageManager.getLaunchIntentForPackage(bedrock)
        if (game == null) { Toast.makeText(this, "Minecraft Bedrock is not installed", Toast.LENGTH_LONG).show(); return }
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        try { startForegroundService(Intent(this, OverlayService::class.java)) }
        catch (t: Throwable) { Toast.makeText(this, "Overlay failed: ${t.javaClass.simpleName}: ${t.message}", Toast.LENGTH_LONG).show() }
        startActivity(game)
    }
}
