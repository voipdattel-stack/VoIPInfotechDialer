package com.voipinfotech.dialer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import com.voipinfotech.dialer.service.SipService
import com.voipinfotech.dialer.sip.SipManager

class VoIPApp : Application() {

    companion object {
        const val CHANNEL_CALL    = "voipinfotech_call"
        const val CHANNEL_MISSED  = "voipinfotech_missed"
        const val CHANNEL_SERVICE = "voipinfotech_service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        SipManager.init(this)
        startSipService()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_CALL, "Incoming Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "VoIP Infotech incoming call alerts"
            enableVibration(true)
            enableLights(true)
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_MISSED, "Missed Calls",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Missed call notifications" })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_SERVICE, "SIP Connection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps VoIP registration active"
            setShowBadge(false)
        })
    }

    private fun startSipService() {
        startForegroundService(Intent(this, SipService::class.java))
    }

    override fun onTerminate() {
        SipManager.destroy()
        super.onTerminate()
    }
}
