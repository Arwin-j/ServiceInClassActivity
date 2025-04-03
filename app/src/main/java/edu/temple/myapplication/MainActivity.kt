package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var timerService: TimerService.TimerBinder
    private var isBound = false

    private lateinit var startPauseButton: Button
    private lateinit var serviceStatusTextView: TextView

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder
            isBound = true

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startPauseButton = findViewById(R.id.startButton)
        serviceStatusTextView = findViewById(R.id.textView)

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)

        startPauseButton.setOnClickListener {

            if (isBound) {
                if (timerService.isRunning) {
                    timerService.pause()
                } else {
                    timerService.start(10)
                }
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isBound) {
                timerService.stop()
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}