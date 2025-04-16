package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private val binder = TimerBinder()
    private var timerHandler: Handler? = null

    private var remainingTime: Long = 0
    private var timerThread: TimerThread? = null

    var isRunning = false
    var paused = false

    inner class TimerBinder : Binder() {
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        val isRunning: Boolean
            get() = this@TimerService.isRunning

        val paused: Boolean
            get() = this@TimerService.paused

        fun start(startValue: Int) {
            val prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
            val savedTime = prefs.getLong("remainingTime", -1L)
            val timeToUse = if (savedTime>0) savedTime else startValue * 1000L

            startTimer(timeToUse)
        }

        fun pause() {
            pauseTimer()
        }

        fun stop() {
            stopTimer()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun startTimer(milliseconds: Long) {
        if (isRunning) return

        remainingTime = milliseconds
        paused = false
        isRunning = true

        timerThread = TimerThread(remainingTime / 1000)
        timerThread?.start()

        // Clear any "paused" flags only AFTER thread has started
        val prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
        prefs.edit().remove("wasPaused").apply()
    }


    private fun pauseTimer() {
        paused = true
        isRunning = false
        timerThread?.interrupt()

        val prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("wasPaused", true)
            .putLong("remainingTime", remainingTime)
            .apply()
    }

    private fun stopTimer() {
        paused = false
        isRunning = false
        timerThread?.interrupt()
        timerThread = null

    }

    inner class TimerThread(private val startSeconds: Long) : Thread() {
        override fun run() {
            try {
                for (i in startSeconds downTo 1) {
                    remainingTime = i * 1000L
                    val prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putLong("remainingTime", remainingTime)
                        .putBoolean("wasPaused", paused) // optional
                        .apply()
                    timerHandler?.sendEmptyMessage(i.toInt())

                    sleep(1000)

                    while (paused) {
                        sleep(100)
                    }
                }
                isRunning = false
                paused = false
                timerHandler?.sendEmptyMessage(0) // timer done
            } catch (e: InterruptedException) {
                Log.d("TimerService", "Timer interrupted")
            }
        }
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }
}
