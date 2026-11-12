package com.sfc.carrozzeria

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        const val DEVICE_BADGE = "CC3L"   // yuqori o'ng burchakdagi yozuv
        const val GOLD = 0xFFE5B96B.toInt()
        const val BG = 0xFF111821.toInt()
        const val CARD = 0xFF1E2833.toInt()
        const val STROKE = 0xFF2F3A47.toInt()
        const val TXT = 0xFFDDE3EE.toInt()
        const val MUTED = 0xFF9AA5B5.toInt()
        const val SEL_BG = 0xFF4A3A24.toInt()
        val ORDER = listOf(SfcMode.OFF, SfcMode.STUDIO, SfcMode.CLUB, SfcMode.HALL, SfcMode.DOME, SfcMode.CONCERT)
    }

    private lateinit var current: TextView
    private val tiles = HashMap<SfcMode, Pair<View, TextView>>()
    private val handler = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun tv(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun box(fill: Int, stroke: Int, radius: Int) = GradientDrawable().apply {
        setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply { setBackgroundColor(Color.BLACK) }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = box(BG, STROKE, 28)
            setPadding(dp(24), dp(24), dp(24), dp(20))
        }
        scroll.addView(card, ViewGroup.MarginLayoutParams(-1, -2).apply {
            setMargins(dp(16), dp(16), dp(16), dp(16))
        })
        scroll.setPadding(dp(16), dp(16), dp(16), dp(16))

        // Header
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(tv("CARROZZERIA", 26f, GOLD, true))
        titles.addView(tv("SFC DIGITAL SOUND FIELD", 13f, MUTED))
        header.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(tv(DEVICE_BADGE, 13f, 0xFF4CAF50.toInt(), true).apply {
            background = box(0xFF1B2E25.toInt(), 0xFF1B2E25.toInt(), 20)
            setPadding(dp(14), dp(6), dp(14), dp(6))
        })
        card.addView(header)
        card.addView(divider())

        // Current mode
        val center = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL }
        center.addView(tv("SOUND FIELD CONTROL", 13f, MUTED).apply { gravity = Gravity.CENTER })
        current = tv("", 34f, GOLD, true).apply { gravity = Gravity.CENTER }
        center.addView(current)
        center.addView(tv("ACTIVE SOUND MODE", 13f, MUTED).apply { gravity = Gravity.CENTER })
        card.addView(center, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(20), 0, dp(20)) })

        // Grid
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val cols = if (landscape) 3 else 2
        val grid = GridLayout(this).apply { columnCount = cols }
        ORDER.forEachIndexed { idx, mode ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                setOnClickListener { select(mode) }
            }
            val name = tv(mode.label, 18f, TXT, true).apply { gravity = Gravity.CENTER }
            val sub = tv("SELECTED", 12f, GOLD).apply { gravity = Gravity.CENTER; visibility = View.INVISIBLE }
            tile.addView(name)
            tile.addView(sub)
            val lp = GridLayout.LayoutParams(
                GridLayout.spec(idx / cols), GridLayout.spec(idx % cols, 1f)
            ).apply {
                width = 0; height = dp(if (landscape) 80 else 92)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            grid.addView(tile, lp)
            tiles[mode] = Pair(tile, name).let { Pair(it.first, sub) }
        }
        card.addView(grid, LinearLayout.LayoutParams(-1, -2))
        card.addView(divider(), LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(16), 0, dp(16)) })

        // Footer
        val footer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        footer.addView(tv("CARROZZERIA SFC", 13f, MUTED), LinearLayout.LayoutParams(0, -2, 1f))
        footer.addView(tv("VERSION 1.0", 13f, MUTED))
        card.addView(footer)

        setContentView(scroll)
        refresh()
        startEngine()   // ilova ochilganda ham effektni qayta ishga tushiradi
    }

    private fun divider() = View(this).apply {
        setBackgroundColor(STROKE)
        layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(16), 0, 0) }
    }

    private fun select(m: SfcMode) {
        Prefs.setMode(this, m)
        refresh()
        startEngine()
        handler.postDelayed({
            if (m != SfcMode.OFF && !Prefs.ok(this)) {
                Toast.makeText(this, "Bu qurilma global audio effektlarni qo'llab-quvvatlamaydi", Toast.LENGTH_LONG).show()
            }
        }, 900)
    }

    private fun startEngine() {
        val i = Intent(this, SfcService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        } catch (t: Throwable) { }
    }

    private fun refresh() {
        val m = Prefs.mode(this)
        current.text = m.label
        tiles.forEach { (mode, pair) ->
            val sel = mode == m
            pair.first.background = if (sel) box(SEL_BG, GOLD, 16) else box(CARD, STROKE, 16)
            pair.second.visibility = if (sel) View.VISIBLE else View.INVISIBLE
            val name = (pair.first as LinearLayout).getChildAt(0) as TextView
            name.setTextColor(if (sel) GOLD else TXT)
        }
    }
}
