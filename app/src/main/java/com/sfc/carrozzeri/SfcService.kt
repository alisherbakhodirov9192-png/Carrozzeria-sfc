package com.sfc.carrozzeria

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.IBinder

class SfcService : Service() {

    private val sets = HashMap<Int, EffectSet>()

    private val sessionReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val s = i.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
            if (s <= 0) return
            when (i.action) {
                AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                    val m = Prefs.mode(c)
                    if (m != SfcMode.OFF) {
                        sets.remove(s)?.release()
                        sets[s] = EffectSet(s).also { it.apply(m) }
                    }
                }
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> sets.remove(s)?.release()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        val f = IntentFilter().apply {
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        }
        registerReceiver(sessionReceiver, f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        applyMode()
        return START_STICKY
    }

    private fun applyMode() {
        val mode = Prefs.mode(this)
        if (mode == SfcMode.OFF) {
            sets.values.forEach { it.release() }
            sets.clear()
            Prefs.setOk(this, true)
            return
        }
        sets.remove(0)?.release()
        val global = EffectSet(0)
        sets[0] = global
        val ok = global.apply(mode)
        sets.filterKeys { it != 0 }.values.forEach { it.apply(mode) }
        Prefs.setOk(this, ok)
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("sfc", "Carrozzeria SFC", NotificationManager.IMPORTANCE_MIN)
            )
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "sfc") else Notification.Builder(this)
        return b.setContentTitle("Carrozzeria SFC")
            .setContentText("Sound field control ishlamoqda")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(open)
            .build()
    }

    override fun onDestroy() {
        try { unregisterReceiver(sessionReceiver) } catch (t: Throwable) { }
        sets.values.forEach { it.release() }
        sets.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
