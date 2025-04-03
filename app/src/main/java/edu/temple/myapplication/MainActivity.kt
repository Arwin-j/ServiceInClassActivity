package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timerTextView: TextView


    private val timerHandler = Handler(Looper.getMainLooper()) { msg ->
        timerTextView.text = "Countdown: ${msg.what}"
        true
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = service as TimerService.TimerBinder
            isBound = true
            timerService?.setHandler(timerHandler)
            updateButtonStates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        timerTextView = findViewById(R.id.textView)


        bindService(Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        startButton.setOnClickListener {
            if (isBound) {
                if (timerService?.isRunning == true) {
                    timerService?.pause()
                    startButton.text = "Resume"
                } else {
                    timerService?.start(10) // Start with 10 seconds
                    startButton.text = "Pause"
                }
            }
        }

        stopButton.setOnClickListener {
            if (isBound) {
                timerService?.stop()
                timerTextView.text = "Timer stopped"
                startButton.text = "Start"
            }
        }
    }

    private fun updateButtonStates() {
        if (isBound) {
            startButton.text = when {
                timerService?.paused == true -> "Resume"
                timerService?.isRunning == true -> "Pause"
                else -> "Start"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
        }
    }
}