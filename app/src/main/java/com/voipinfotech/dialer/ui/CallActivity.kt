package com.voipinfotech.dialer.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.voipinfotech.dialer.databinding.ActivityCallBinding
import com.voipinfotech.dialer.sip.SipManager
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class CallActivity : AppCompatActivity(), SipManager.SipListener {

    companion object {
        const val EXTRA_REMOTE_NUMBER = "remote_number"
        const val EXTRA_IS_OUTGOING   = "is_outgoing"
    }

    private lateinit var binding: ActivityCallBinding
    private var callSeconds = 0
    private val timerHandler  = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var isMuted   = false
    private var isSpeaker = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val remoteNumber = intent.getStringExtra(EXTRA_REMOTE_NUMBER) ?: "Unknown"
        val isOutgoing   = intent.getBooleanExtra(EXTRA_IS_OUTGOING, true)

        binding.tvRemote.text   = remoteNumber
        binding.tvStatus.text   = if (isOutgoing) "Calling…" else "Connecting…"
        binding.tvDuration.text = "00:00"

        SipManager.setListener(this)

        binding.btnMute.setOnClickListener { toggleMute() }
        binding.btnSpeaker.setOnClickListener { toggleSpeaker() }
        binding.btnHangup.setOnClickListener {
            SipManager.hangup()
            finishCall()
        }
    }

    private fun startTimer() {
        callSeconds = 0
        timerRunnable = object : Runnable {
            override fun run() {
                callSeconds++
                val m = callSeconds / 60
                val s = callSeconds % 60
                binding.tvDuration.text = "%02d:%02d".format(m, s)
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.postDelayed(timerRunnable!!, 1000)
    }

    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    private fun toggleMute() {
        isMuted = SipManager.toggleMute()
        binding.btnMute.alpha = if (isMuted) 0.5f else 1.0f
        binding.tvMuteLabel.text = if (isMuted) "Unmute" else "Mute"
    }

    private fun toggleSpeaker() {
        isSpeaker = SipManager.toggleSpeaker()
        binding.btnSpeaker.alpha = if (isSpeaker) 1.0f else 0.5f
        binding.tvSpeakerLabel.text = if (isSpeaker) "Speaker" else "Earpiece"
    }

    private fun finishCall() {
        stopTimer()
        SipManager.setListener(null)
        finish()
    }

    override fun onRegistrationStateChanged(state: RegistrationState, message: String) {}

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        runOnUiThread {
            when (state) {
                Call.State.Connected, Call.State.StreamsRunning -> {
                    binding.tvStatus.text = "In Call"
                    if (callSeconds == 0) startTimer()
                }
                Call.State.End, Call.State.Released, Call.State.Error -> {
                    finishCall()
                }
                Call.State.Paused -> binding.tvStatus.text = "On Hold"
                Call.State.OutgoingRinging -> binding.tvStatus.text = "Ringing…"
                else -> {}
            }
        }
    }

    override fun onIncomingCall(call: Call) {}

    override fun onBackPressed() {
        // Block back — user must hang up explicitly
    }

    override fun onDestroy() {
        stopTimer()
        SipManager.setListener(null)
        super.onDestroy()
    }
}
