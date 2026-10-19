package com.jokinojewels.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.jokinojewels.game.R
import java.util.Collections

enum class Sfx {
    CLICK, CORRECT, WRONG, MATCH, WIN, LOSE
}

/**
 * Short UI sound effects through [SoundPool]. Created once in the Application.
 * Never throws: if audio cannot be initialised the game simply stays silent.
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    } catch (e: Exception) {
        null
    }

    private val soundIds = mutableMapOf<Sfx, Int>()
    private val loadedIds: MutableSet<Int> = Collections.synchronizedSet(mutableSetOf())

    @Volatile
    private var enabled: Boolean = true

    init {
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds.add(sampleId)
        }
        Sfx.entries.forEach { sfx ->
            try {
                val id = soundPool?.load(appContext, resourceFor(sfx), 1) ?: 0
                if (id != 0) soundIds[sfx] = id
            } catch (e: Exception) {
                // Missing or unreadable resource: that effect stays silent.
            }
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    fun play(sfx: Sfx) {
        if (!enabled) return
        val pool = soundPool ?: return
        val id = soundIds[sfx] ?: return
        if (id !in loadedIds) return
        pool.play(id, VOLUME, VOLUME, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
    }

    private fun resourceFor(sfx: Sfx): Int = when (sfx) {
        Sfx.CLICK -> R.raw.click
        Sfx.CORRECT -> R.raw.correct
        Sfx.WRONG -> R.raw.wrong
        Sfx.MATCH -> R.raw.match
        Sfx.WIN -> R.raw.win
        Sfx.LOSE -> R.raw.lose
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val VOLUME = 0.8f
    }
}
