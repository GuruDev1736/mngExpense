package com.guruprasad.mngexpense.data.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/**
 * Detects a deliberate shake from accelerometer data.
 *
 * A single bump (putting the phone on a table, a step while walking) is ignored:
 * we need [requiredSpikes] force spikes above [thresholdG] within [windowMs].
 * After a shake fires, further shakes are ignored for [cooldownMs].
 */
class ShakeDetector(
    @Volatile var thresholdG: Float = 2.7f,
    private val requiredSpikes: Int = 3,
    private val windowMs: Long = 1000,
    private val minSpikeGapMs: Long = 100,
    private val cooldownMs: Long = 2000,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var spikeCount = 0
    private var firstSpikeAt = 0L
    private var lastSpikeAt = 0L
    private var lastShakeAt = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(x * x + y * y + z * z)   // ≈1.0 when the phone is still

        if (gForce < thresholdG) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastShakeAt < cooldownMs) return
        if (now - lastSpikeAt < minSpikeGapMs) return   // same spike, several samples

        if (spikeCount == 0 || now - firstSpikeAt > windowMs) {
            spikeCount = 0
            firstSpikeAt = now
        }
        spikeCount++
        lastSpikeAt = now

        if (spikeCount >= requiredSpikes) {
            spikeCount = 0
            lastShakeAt = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}