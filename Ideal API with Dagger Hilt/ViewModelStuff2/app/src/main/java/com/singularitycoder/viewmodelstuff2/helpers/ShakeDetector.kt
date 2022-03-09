package com.singularitycoder.viewmodelstuff2.helpers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

// https://stackoverflow.com/questions/2317428/how-to-refresh-app-upon-shaking-the-device
// https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android
class ShakeDetector : SensorEventListener {

    companion object {
        /**
         * The gForce that is necessary to register as shake.
         * Must be greater than 1G (one earth gravity unit).
         * You can install "G-Force", by Blake La Pierre
         * from the Google Play Store and run it to see how
         *  many G's it takes to register a shake
         */
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000
    }

    private var shakeListener: OnShakeListener? = null
    private var shakeTimestamp: Long = 0
    private var shakeCount = 0

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        shakeListener ?: return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH
        val gForce: Float = sqrt(gX * gX + gY * gY + gZ * gZ) // gForce will be close to 1 when there is no movement.

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) return // ignore shake events too close to each other (500ms)
            if (shakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0 // reset the shake count after 3 seconds of no shakes
            }
            shakeTimestamp = now
            shakeCount++
            shakeListener!!.onShake(shakeCount)
        }
    }

    fun setOnShakeListener(listener: OnShakeListener?) {
        this.shakeListener = listener
    }

    interface OnShakeListener {
        fun onShake(count: Int)
    }
}
