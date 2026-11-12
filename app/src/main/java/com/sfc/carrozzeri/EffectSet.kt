package com.sfc.carrozzeria

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer

/** Bitta audio sessiya (0 = global) uchun effektlar to'plami. */
class EffectSet(private val session: Int) {
    private var eq: Equalizer? = try { Equalizer(1000, session) } catch (e: Throwable) { null }
    private var bass: BassBoost? = try { BassBoost(1000, session) } catch (e: Throwable) { null }
    private var virt: Virtualizer? = try { Virtualizer(1000, session) } catch (e: Throwable) { null }
    private var rev: PresetReverb? = try { PresetReverb(1000, session) } catch (e: Throwable) { null }

    /** Kamida bitta effekt ishga tushsa true qaytaradi. */
    fun apply(mode: SfcMode): Boolean {
        var any = false

        eq?.let { e ->
            try {
                val r = e.bandLevelRange
                val n = e.numberOfBands.toInt()
                for (i in 0 until n) {
                    val pos = if (n > 1) i.toFloat() / (n - 1) else 0f
                    val g = mode.curve(pos).coerceIn(-1f, 1f)
                    val lvl = if (g >= 0) r[1] * g else r[0] * (-g)
                    e.setBandLevel(i.toShort(), lvl.toInt().coerceIn(r[0].toInt(), r[1].toInt()).toShort())
                }
                e.enabled = true
                any = true
            } catch (t: Throwable) { }
        }

        bass?.let { b ->
            try {
                if (b.strengthSupported) b.setStrength(mode.bass.toShort())
                b.enabled = mode.bass > 0
                any = true
            } catch (t: Throwable) { }
        }

        virt?.let { v ->
            try {
                if (v.strengthSupported) v.setStrength(mode.virt.toShort())
                v.enabled = mode.virt > 0
                any = true
            } catch (t: Throwable) { }
        }

        rev?.let { r ->
            try {
                val p = mode.reverb
                if (p != null) { r.preset = p; r.enabled = true } else r.enabled = false
                any = true
            } catch (t: Throwable) { }
        }
        return any
    }

    fun release() {
        try { eq?.release() } catch (t: Throwable) { }
        try { bass?.release() } catch (t: Throwable) { }
        try { virt?.release() } catch (t: Throwable) { }
        try { rev?.release() } catch (t: Throwable) { }
        eq = null; bass = null; virt = null; rev = null
    }
}
