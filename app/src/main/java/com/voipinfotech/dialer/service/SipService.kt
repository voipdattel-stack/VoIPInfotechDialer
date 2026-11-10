package com.voipinfotech.dialer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.voipinfotech.dialer.R
import com.voipinfotech.dialer.VoIPApp
import com.voipinfotech.dialer.sip.SipManager
import com.voipinfotech.dialer.ui.IncomingCallActivity
import com.voipinfotech.dialer.ui.MainActivity
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class SipService : Service(), SipManager.SipListener {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID_SERVICE, buildServiceNotification())
        SipManager.setListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onRegistrationStateChanged(state: RegistrationState, message: String) {
        // Optionally update notification to show registration status
    }

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        if (state == Call.State.End || state == Call.State.Released) {
            // Call ended — nothing to do in service
        }
    }

    override fun onIncomingCall(call: Call) {
        val caller = call.remoteAddress?.displayName
            ?: call.remoteAddress?.username
            ?: "Unknown"
        showIncomingCallNotification(caller)

        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        startActivity(intent)
    }

    private fun showIncomingCallNotification(caller: String) {
        val fullScreenIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, IncomingCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(this, VoIPApp.CHANNEL_CALL)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("Incoming Call")
            .setContentText(caller)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(false)
            .build()
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIF_ID_INCOMING, notif)
    }

    private fun buildServiceNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, VoIPApp.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("VoIP Infotech")
            .setContentText("SIP connection active")
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        SipManager.setListener(null)
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID_SERVICE  = 1001
        private const val NOTIF_ID_INCOMING = 1002
    }
}
