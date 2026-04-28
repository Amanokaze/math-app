@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.mathapp.practice.ui

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSURL

private val players = HashMap<SoundEffect, AVAudioPlayer>()
private var playersLoaded = false

private val soundFiles = mapOf(
    SoundEffect.Tap           to "sfx_tap",
    SoundEffect.Correct       to "sfx_correct",
    SoundEffect.Incorrect     to "sfx_incorrect",
    SoundEffect.Reward        to "sfx_reward",
    SoundEffect.Complete      to "sfx_complete",
    SoundEffect.Back          to "sfx_back",
    SoundEffect.CountdownTick to "sfx_countdown_tick",
    SoundEffect.CountdownGo   to "sfx_countdown_go"
)

private fun loadPlayers() {
    if (playersLoaded) return
    playersLoaded = true
    soundFiles.forEach { (effect, name) ->
        val url: NSURL? = NSBundle.mainBundle.URLForResource(name, withExtension = "wav")
            ?: return@forEach
        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val player = AVAudioPlayer(contentsOfURL = url!!, error = errorPtr.ptr)
            if (errorPtr.value == null && player != null) {
                player.prepareToPlay()
                players[effect] = player
            }
        }
    }
}

actual object SoundPlayer {
    actual fun play(effect: SoundEffect) {
        if (AppSettings.getInt("soundEffectsEnabled", 1) == 0) return
        loadPlayers()
        val player = players[effect] ?: return
        if (player.isPlaying()) player.currentTime = 0.0
        player.play()
    }
}
