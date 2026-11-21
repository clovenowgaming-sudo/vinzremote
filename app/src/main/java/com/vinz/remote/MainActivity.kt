package com.vinz.remote

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        val status = TextView(this).apply {
            textSize = 16f
            text = "VinzRemote\n\n" +
                "1. Enable accessibility service dulu (tombol di bawah)\n" +
                "2. Start server (tombol di bawah)\n\n" +
                "Server lokal bakal jalan di 127.0.0.1:8765.\n" +
                "MCP server di Termux yang manggil endpoint ini dan di-expose ke internet lewat cloudflared quick tunnel."
        }

        val btnAccessibility = Button(this).apply {
            text = "Buka Settings Accessibility"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val btnStart = Button(this).apply {
            text = "Start Server"
            setOnClickListener {
                startForegroundService(Intent(this@MainActivity, RemoteServerService::class.java))
                status.append("\n\nServer di-start.")
            }
        }

        val btnStop = Button(this).apply {
            text = "Stop Server"
            setOnClickListener {
                stopService(Intent(this@MainActivity, RemoteServerService::class.java))
                status.append("\n\nServer di-stop.")
            }
        }

        root.addView(status)
        root.addView(btnAccessibility)
        root.addView(btnStart)
        root.addView(btnStop)
        setContentView(root)
    }
}
