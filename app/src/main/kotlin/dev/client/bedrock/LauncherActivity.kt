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
        root.addView(Button(this).apply { text = "Export Minecraft APKs to Downloads"; setOnClickListener { export() } })
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

    private fun export() {
        Toast.makeText(this, "Exporting, this can take a minute...", Toast.LENGTH_LONG).show()
        Thread {
            try {
                val ai = packageManager.getApplicationInfo(bedrock, 0)
                val files = listOf(ai.sourceDir) + (ai.splitSourceDirs?.toList() ?: emptyList())
                for (path in files) {
                    val src = java.io.File(path)
                    val v = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "mc_" + src.name)
                        put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download/BedrockExport")
                    }
                    val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, v)!!
                    contentResolver.openOutputStream(uri)!!.use { o -> src.inputStream().use { it.copyTo(o) } }
                }
                runOnUiThread { Toast.makeText(this, "Done: ${files.size} files in Downloads/BedrockExport", Toast.LENGTH_LONG).show() }
            } catch (t: Throwable) {
                runOnUiThread { Toast.makeText(this, "Export failed: ${t.javaClass.simpleName}: ${t.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }
}
