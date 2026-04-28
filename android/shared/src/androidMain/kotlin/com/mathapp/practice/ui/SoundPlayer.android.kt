package com.mathapp.practice.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

private var soundPool: SoundPool? = null
private val soundIds = HashMap<SoundEffect, Int>()
// Tracks which SoundPool sample IDs have finished loading (load is async).
// play() is silently ignored for unloaded IDs on slow/emulator devices.
private val loadedIds = HashSet<Int>()

fun initSoundPlayer(context: Context) {
    // USAGE_MEDIA instead of USAGE_GAME: emulators don't virtualise game audio
    // hardware, so USAGE_GAME is silently dropped. USAGE_MEDIA routes correctly
    // on both emulator and real devices.
    val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(attrs)
        .build()
    soundPool = pool

    pool.setOnLoadCompleteListener { _, sampleId, status ->
        if (status == 0) loadedIds.add(sampleId)
    }

    // R.raw.* in library modules has value 0 at runtime (nonTransitiveRClass=true,
    // no static initializer in the library's R class). Use getIdentifier() to look
    // up the resource IDs from the app's merged resource table at runtime.
    val res = context.resources
    val pkg = context.packageName
    mapOf(
        SoundEffect.Tap           to "sfx_tap",
        SoundEffect.Correct       to "sfx_correct",
        SoundEffect.Incorrect     to "sfx_incorrect",
        SoundEffect.Reward        to "sfx_reward",
        SoundEffect.Complete      to "sfx_complete",
        SoundEffect.Back          to "sfx_back",
        SoundEffect.CountdownTick to "sfx_countdown_tick",
        SoundEffect.CountdownGo   to "sfx_countdown_go"
    ).forEach { (effect, name) ->
        val resId = res.getIdentifier(name, "raw", pkg)
        if (resId != 0) soundIds[effect] = pool.load(context.applicationContext, resId, 1)
    }
}

actual object SoundPlayer {
    actual fun play(effect: SoundEffect) {
        if (AppSettings.getInt("soundEffectsEnabled", 1) == 0) return
        val pool = soundPool ?: return
        val id = soundIds[effect] ?: return
        if (id > 0 && loadedIds.contains(id)) pool.play(id, 1f, 1f, 1, 0, 1f)
    }
}
