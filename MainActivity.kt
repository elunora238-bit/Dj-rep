package com.example.djmixpro

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.Equalizer
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : Activity() {
    private var playerA: MediaPlayer? = null
    private var playerB: MediaPlayer? = null
    private var eqA: Equalizer? = null
    private var eqB: Equalizer? = null
    private lateinit var status: TextView
    private lateinit var crossfader: SeekBar
    private lateinit var volA: SeekBar
    private lateinit var volB: SeekBar
    private val names = arrayOf("DECK A", "DECK B")
    private val tracks = arrayOf("Sin pista cargada", "Sin pista cargada")
    private val trackViews = arrayOfNulls<TextView>(2)
    private val cuePositions = intArrayOf(0, 0)
    private val cueSet = booleanArrayOf(false, false)
    private val loopEnabled = booleanArrayOf(false, false)

    private val pickA = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { loadTrack(0, it) } }
    private val pickB = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { loadTrack(1, it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        buildUi()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun panel(): GradientDrawable = GradientDrawable().apply {
        setColor(Color.rgb(20, 26, 38)); cornerRadius = dp(16).toFloat()
        setStroke(dp(1), Color.rgb(48, 62, 82))
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            setStroke(dp(1), Color.argb(55, 255, 255, 255))
        }

    private fun label(text: String, size: Float, color: Int = Color.WHITE): TextView =
        TextView(this).apply { this.text = text; textSize = size; setTextColor(color); typeface = Typeface.DEFAULT_BOLD }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setBackgroundColor(Color.rgb(8, 11, 18))
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; orientation = LinearLayout.HORIZONTAL }
        header.addView(label("DJ MIX PRO", 25f, Color.rgb(25, 211, 255)), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(label("PRO DJ SYSTEM  •  2 DECKS", 11f, Color.rgb(155, 174, 198)))
        root.addView(header)

        val decks = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        decks.addView(deckView(0), LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(dp(0), dp(10), dp(6), dp(8)) })
        decks.addView(deckView(1), LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(dp(6), dp(10), dp(0), dp(8)) })
        root.addView(decks, LinearLayout.LayoutParams(-1, 0, 1f))

        val mixer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = panel(); setPadding(dp(12), dp(8), dp(12), dp(8))
            elevation = dp(3).toFloat()
        }
        mixer.addView(label("MASTER MIXER", 13f, Color.rgb(25, 211, 255)))
        val crossRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        crossRow.addView(label("A", 12f, Color.rgb(25,211,255)))
        crossfader = SeekBar(this).apply { max = 100; progress = 50 }
        crossRow.addView(crossfader, LinearLayout.LayoutParams(0, dp(32), 1f))
        crossRow.addView(label("B", 12f, Color.rgb(255,150,50)))
        mixer.addView(crossRow)
        crossfader.setOnSeekBarChangeListener(simpleSeek { updateVolumes() })
        val eqRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        eqRow.addView(eqPanel("DECK A EQ", 0), LinearLayout.LayoutParams(0, -2, 1f))
        eqRow.addView(eqPanel("DECK B EQ", 1), LinearLayout.LayoutParams(0, -2, 1f))
        mixer.addView(eqRow)
        root.addView(mixer)
        status = label("Listo para mezclar · Carga una canción en cada deck", 11f, Color.LTGRAY)
        status.gravity = Gravity.CENTER
        root.addView(status)
        setContentView(root)
    }

    private fun deckView(index: Int): View {
        val color = if (index == 0) Color.rgb(25, 211, 255) else Color.rgb(255, 150, 50)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = panel(); setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        box.addView(label(if (index == 0) "01  •  DECK A" else "02  •  DECK B", 17f, color))
        val track = label(tracks[index], 11f, Color.WHITE)
        track.maxLines = 2
        trackViews[index] = track
        box.addView(track)
        val load = Button(this).apply {
            text = "＋  LOAD TRACK"
            textSize = 11f
            setTextColor(Color.WHITE)
            background = rounded(if (index == 0) Color.rgb(0, 112, 145) else Color.rgb(153, 76, 20),  dp(9))
            setOnClickListener { if (index == 0) pickA.launch(arrayOf("audio/*")) else pickB.launch(arrayOf("audio/*")) }
        }
        box.addView(load)
        val wheel = TextView(this).apply {
            text = "◉"; textSize = 66f; gravity = Gravity.CENTER; setTextColor(color)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(10, 14, 22))
                setStroke(dp(7), color)
            }
            elevation = dp(5).toFloat()
        }
        box.addView(wheel, LinearLayout.LayoutParams(dp(132), dp(132)).apply { gravity = Gravity.CENTER; topMargin = dp(8); bottomMargin = dp(8) })
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        val play = Button(this).apply {
            text = "▶  PLAY / PAUSE"; textSize = 10f
            setTextColor(Color.WHITE); background = rounded(Color.rgb(0, 132, 112), dp(9))
            setOnClickListener { togglePlay(index) }
        }
        val stop = Button(this).apply {
            text = "■"; setTextColor(Color.WHITE); background = rounded(Color.rgb(74, 83, 99), dp(9))
            setOnClickListener { stopDeck(index) }
        }
        controls.addView(play); controls.addView(stop); box.addView(controls)
        val volume = SeekBar(this).apply { max = 100; progress = 80 }
        if (index == 0) volA = volume else volB = volume
        volume.setOnSeekBarChangeListener(simpleSeek { updateVolumes() })
        box.addView(label("VOLUME", 10f, Color.LTGRAY)); box.addView(volume)

        val pitchTitle = label("PITCH  0%", 10f, Color.LTGRAY)
        box.addView(pitchTitle)
        val pitch = SeekBar(this).apply { max = 100; progress = 50 }
        pitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = 0.5f + progress / 100f
                pitchTitle.text = "PITCH  ${if (speed >= 1f) "+" else ""}${"%.0f".format((speed - 1f) * 100)}%  ·  ${"%.2f".format(speed)}x"
                setDeckSpeed(index, speed)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        box.addView(pitch)

        val effects = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        listOf("CUE", "LOOP", "SYNC").forEach { effectName ->
            effects.addView(Button(this).apply {
                text = effectName
                textSize = 10f
                setTextColor(Color.WHITE)
                background = rounded(Color.rgb(43, 54, 72), dp(8))
                setOnClickListener {
                    val p = if (index == 0) playerA else playerB
                    when (effectName) {
                        "CUE" -> {
                            if (p == null) status.text = "Carga una pista en ${names[index]} primero"
                            else if (!cueSet[index]) {
                                cuePositions[index] = p.currentPosition
                                cueSet[index] = true
                                status.text = "${names[index]}: punto CUE guardado"
                            } else {
                                p.seekTo(cuePositions[index])
                                status.text = "${names[index]}: regreso al punto CUE"
                            }
                        }
                        "LOOP" -> {
                            if (p == null) status.text = "Carga una pista en ${names[index]} primero"
                            else {
                                loopEnabled[index] = !loopEnabled[index]
                                p.isLooping = loopEnabled[index]
                                text = if (loopEnabled[index]) "LOOP ON" else "LOOP"
                                status.text = "${names[index]}: repetición ${if (loopEnabled[index]) "activada" else "desactivada"}"
                            }
                        }
                        else -> status.text = "SYNC BPM estará disponible en una versión avanzada; MediaPlayer no calcula BPM."
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(40), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        box.addView(effects)
        return box
    }

    private fun eqPanel(title: String, deck: Int): View {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(4), 0, dp(4), 0) }
        col.addView(label(title, 10f, Color.LTGRAY))
        listOf("HIGH", "MID", "LOW").forEachIndexed { band, name ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(label(name, 9f, Color.LTGRAY), LinearLayout.LayoutParams(dp(42), -2))
            val seek = SeekBar(this).apply { max = 100; progress = 50 }
            seek.setOnSeekBarChangeListener(simpleSeek { applyEq(deck, band, seek.progress) })
            row.addView(seek, LinearLayout.LayoutParams(0, dp(28), 1f))
            col.addView(row)
        }
        return col
    }

    private fun simpleSeek(action: () -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { action() }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun loadTrack(deck: Int, uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
        try {
            val mp = MediaPlayer().apply {
                setDataSource(this@MainActivity, uri)
                setOnPreparedListener {
                    if (deck == 0) {
                        playerA?.release(); playerA = this
                        tracks[0] = queryName(uri)
                        trackViews[0]?.text = tracks[0]
                        cueSet[0] = false
                        loopEnabled[0] = false
                        eqA?.release(); eqA = Equalizer(0, audioSessionId).apply { enabled = true }
                    } else {
                        playerB?.release(); playerB = this
                        tracks[1] = queryName(uri)
                        trackViews[1]?.text = tracks[1]
                        cueSet[1] = false
                        loopEnabled[1] = false
                        eqB?.release(); eqB = Equalizer(0, audioSessionId).apply { enabled = true }
                    }
                    status.text = "${names[deck]} listo: ${tracks[deck]}"
                    updateVolumes()
                }
                setOnErrorListener { _, what, extra -> status.text = "Error de audio ($what/$extra)"; true }
                prepareAsync()
            }
        } catch (e: Exception) { status.text = "No se pudo cargar el archivo: ${e.message}" }
    }

    private fun queryName(uri: Uri): String {
        var displayName: String? = null
        try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) displayName = cursor.getString(index)
                }
            }
        } catch (_: Exception) {}
        return displayName ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Track cargado"
    }

    private fun togglePlay(deck: Int) {
        val p = if (deck == 0) playerA else playerB
        if (p == null) { status.text = "Primero carga un track en ${names[deck]}"; return }
        try { if (p.isPlaying) p.pause() else p.start() } catch (_: Exception) {}
    }
    private fun stopDeck(deck: Int) {
        val p = if (deck == 0) playerA else playerB
        try { p?.pause(); p?.seekTo(0) } catch (_: Exception) {}
    }
    private fun updateVolumes() {
        if (!::crossfader.isInitialized || !::volA.isInitialized || !::volB.isInitialized) return
        // Curva equal-power: mantiene una sensación de volumen más uniforme al cruzar.
        val x = crossfader.progress / 100f
        val a = kotlin.math.cos(x * Math.PI / 2.0).toFloat() * (volA.progress / 100f)
        val b = kotlin.math.sin(x * Math.PI / 2.0).toFloat() * (volB.progress / 100f)
        playerA?.setVolume(a, a)
        playerB?.setVolume(b, b)
    }

    private fun setDeckSpeed(deck: Int, speed: Float) {
        val p = if (deck == 0) playerA else playerB
        if (p == null) {
            status.text = "Carga una canción en ${names[deck]} para ajustar PITCH"
            return
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                val params = PlaybackParams().setSpeed(speed).setPitch(1.0f)
                p.playbackParams = params
            }
        } catch (e: Exception) {
            status.text = "No se pudo cambiar la velocidad: ${e.message ?: "formato no compatible"}"
        }
    }
    private fun applyEq(deck: Int, band: Int, value: Int) {
        val eq = if (deck == 0) eqA else eqB
        try {
            if (eq != null && eq.numberOfBands > 0) {
                val targetHz = when (band) { 0 -> 8000; 1 -> 1000; else -> 100 }
                var chosen = 0
                var bestDistance = Int.MAX_VALUE
                for (i in 0 until eq.numberOfBands) {
                    val centerHz = eq.getCenterFreq(i.toShort()) / 1000
                    val distance = kotlin.math.abs(centerHz - targetHz)
                    if (distance < bestDistance) { bestDistance = distance; chosen = i }
                }
                val range = eq.bandLevelRange
                val level = (range[0] + (range[1] - range[0]) * value / 100).toShort()
                eq.setBandLevel(chosen.toShort(), level)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        try { playerA?.release(); playerB?.release(); eqA?.release(); eqB?.release() } catch (_: Exception) {}
    }
}
