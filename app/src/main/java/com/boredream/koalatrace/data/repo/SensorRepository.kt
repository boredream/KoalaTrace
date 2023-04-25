package com.boredream.koalatrace.data.repo

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.SystemClock
import com.boredream.koalatrace.data.constant.LocationConstant
import com.boredream.koalatrace.data.repo.source.LinearAccelerationDataSource
import com.boredream.koalatrace.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SensorRepository @Inject constructor(
    private val logger: Logger,
    private val dataSource: LinearAccelerationDataSource
) {

    // 开始监听手机是否再次开始移动
    private var determineMovementStatus = 0
    private var startDetermineMovementTime = 0L
    private var movementCount = 0
    private var notMovementCount = 0

    var movementListener: (move: Boolean) -> Unit = { }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val acceleration = calculateAcceleration(event)
            val move = handleSensorChanged(acceleration)
            movementListener.invoke(move)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    fun startListenerMovement() {
        dataSource.startListenerMovement(sensorListener)
    }

    fun stopListenerMovement() {
        dataSource.stopListenerMovement(sensorListener)
    }

    private fun calculateAcceleration(event: SensorEvent?): Double {
        if (event == null) return 0.0
        val x = event.values[0]
        // 噪音
        if (determineMovementStatus == 0 && x < 0.1) return 0.0

        val y = event.values[1]
        val z = event.values[2]
        return sqrt((x * x + y * y + z * z).toDouble())
    }

    fun handleSensorChanged(acceleration: Double) : Boolean {
        if (acceleration > LocationConstant.DETERMINE_MOVEMENT_THRESHOLD
            && determineMovementStatus == 0) {
            // 如果达到阈值，开始检测
            startDetermineMovementTime = SystemClock.elapsedRealtime()
            determineMovementStatus = 1
            movementCount = 0
            notMovementCount = 0
            logger.v("start determine movement")
        }

        if (determineMovementStatus == 1) {
            // 如果已经开始检测了，开始记录运动状态
            if (acceleration > LocationConstant.DETERMINE_MOVEMENT_THRESHOLD) {
                movementCount++
            } else {
                notMovementCount++
            }
            logger.v("determine movement [$movementCount , $notMovementCount]")
        }

        if (determineMovementStatus == 1) {
            val duration = SystemClock.elapsedRealtime() - startDetermineMovementTime
            if (duration > LocationConstant.DETERMINE_MOVEMENT_CHECK_INTERVAL) {
                // 每次达到时间间隔进行一次判定
                determineMovementStatus = 0
                if (notMovementCount == 0) notMovementCount = 1 // 方便除法
                if (1f * movementCount / notMovementCount > 7) {
                    // 如果区间内多数检测结果是移动状态，则判断用户已经切换到移动
                    logger.i("determine movement!")
                    return true;
                } else {
                    logger.i("determine not movement")
                }
            }
        }
        return false
    }
}