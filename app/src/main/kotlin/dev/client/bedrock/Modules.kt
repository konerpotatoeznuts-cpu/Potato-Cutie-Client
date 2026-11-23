package dev.client.bedrock

import android.content.Context

data class Module(val id: String, val name: String, val desc: String)

/** Module registry. Toggle state is stored in SharedPreferences so a native hook layer can read it later. */
object Modules {
    val all = listOf(
        Module("auto_sprint", "Auto-Sprint", "Keep sprinting while moving"),
        Module("fullbright", "Fullbright", "Max light level everywhere"),
        Module("zoom", "Zoom", "Hold-to-zoom camera"),
        Module("coords_hud", "Coordinates HUD", "Show XYZ on screen"),
        Module("fps_hud", "FPS HUD", "Show frame rate on screen"),
    )
    private fun prefs(c: Context) = c.getSharedPreferences("modules", Context.MODE_PRIVATE)
    fun isOn(c: Context, id: String) = prefs(c).getBoolean(id, false)
    fun set(c: Context, id: String, on: Boolean) = prefs(c).edit().putBoolean(id, on).apply()
}
