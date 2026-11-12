package com.sfc.carrozzeria

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("sfc", Context.MODE_PRIVATE)

    fun mode(c: Context): SfcMode =
        try { SfcMode.valueOf(sp(c).getString("mode", "STUDIO")!!) } catch (e: Exception) { SfcMode.STUDIO }

    fun setMode(c: Context, m: SfcMode) = sp(c).edit().putString("mode", m.name).apply()

    fun ok(c: Context) = sp(c).getBoolean("ok", true)
    fun setOk(c: Context, v: Boolean) = sp(c).edit().putBoolean("ok", v).apply()
}
