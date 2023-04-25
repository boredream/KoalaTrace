package com.boredream.koalatrace.data.repo.source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LinearAccelerationDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var manager: SensorManager? = null
    private var sensor: Sensor? = null

    fun startListenerMovement(listener: SensorEventListener) {
        if (manager == null || sensor == null) {
            manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensor = manager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        manager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListenerMovement(listener: SensorEventListener) {
        manager?.unregisterListener(listener, sensor)
    }

}