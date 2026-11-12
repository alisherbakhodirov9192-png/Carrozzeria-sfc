package com.sfc.carrozzeria

import android.media.audiofx.PresetReverb

/**
 * bass: BassBoost kuchi (0..1000)
 * virt: Virtualizer kuchi (0..1000)  -> "sahna kengligi"
 * reverb: PresetReverb preseti
 * curve: ichki ekvalayzer egri chizig'i. Kirish 0..1 (past -> yuqori chastota),
 *        chiqish -1..1 (polosa diapazoniga nisbatan kuchaytirish).
 */
enum class SfcMode(
    val label: String,
    val bass: Int,
    val virt: Int,
    val reverb: Short?,
    val curve: (Float) -> Float
) {
    OFF("OFF", 0, 0, null, { 0f }),
    STUDIO("STUDIO", 0, 250, PresetReverb.PRESET_SMALLROOM, { p -> if (p > 0.6f) 0.1f else 0f }),
    CLUB("CLUB", 750, 400, PresetReverb.PRESET_MEDIUMROOM, { p ->
        if (p < 0.3f) 0.5f else if (p > 0.8f) 0.2f else -0.1f
    }),
    HALL("HALL", 200, 650, PresetReverb.PRESET_LARGEHALL, { p -> if (p > 0.5f) 0.15f else 0.05f }),
    DOME("DOME", 350, 1000, PresetReverb.PRESET_LARGEROOM, { p -> 0.25f * (1f - p) + 0.1f }),
    CONCERT("CONCERT", 500, 850, PresetReverb.PRESET_LARGEHALL, { p ->
        0.1f + 0.35f * Math.abs(p - 0.5f) * 2f
    });
}
