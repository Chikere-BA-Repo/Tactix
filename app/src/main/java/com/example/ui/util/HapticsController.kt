package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class TactixHaptic {
    MOVE_TAP,
    DECAY_SWAP,
    HINT_REVEAL,
    ERROR_SHAKE,
    VICTORY,
    DRAW,
}

class HapticsController(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun perform(type: TactixHaptic, enabled: Boolean) {
        if (!enabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    TactixHaptic.MOVE_TAP -> VibrationEffect.createOneShot(18L, 110)
                    TactixHaptic.DECAY_SWAP -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 14L, 35L, 22L),
                        intArrayOf(0, 75, 0, 140),
                        -1,
                    )
                    TactixHaptic.HINT_REVEAL -> VibrationEffect.createOneShot(24L, 90)
                    TactixHaptic.ERROR_SHAKE -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 25L, 45L, 30L),
                        intArrayOf(0, 140, 0, 160),
                        -1,
                    )
                    TactixHaptic.VICTORY -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 28L, 55L, 28L, 55L, 50L),
                        intArrayOf(0, 120, 0, 165, 0, 220),
                        -1,
                    )
                    TactixHaptic.DRAW -> VibrationEffect.createOneShot(36L, 100)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val duration = when (type) {
                    TactixHaptic.MOVE_TAP -> 18L
                    TactixHaptic.DECAY_SWAP -> 28L
                    TactixHaptic.HINT_REVEAL -> 22L
                    TactixHaptic.ERROR_SHAKE -> 45L
                    TactixHaptic.VICTORY -> 70L
                    TactixHaptic.DRAW -> 35L
                }
                vib.vibrate(duration)
            }
        } catch (_: SecurityException) {
            // Gracefully ignore if platform blocks vibration
        }
    }
}
