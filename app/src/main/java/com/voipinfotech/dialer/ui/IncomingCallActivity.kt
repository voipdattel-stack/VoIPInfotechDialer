package com.voipinfotech.dialer.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.voipinfotech.dialer.databinding.ActivityIncomingBinding
import com.voipinfotech.dialer.sip.SipManager
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class IncomingCallActivity : AppCompatActivity(), SipManager.SipListener {

    private lateinit var binding: ActivityIncomingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        binding = ActivityIncomingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val call = SipManager.getCurrentCall()
        val caller = call?.remoteAddress?.displayName
            ?: call?.remoteAddress?.username
            ?: "Unknown Caller"
        binding.tvCaller.text = caller

        SipManager.setListener(this)

        binding.btnAccept.setOnClickListener {
            call?.let { SipManager.answerCall(it) }
            startActivity(Intent(this, CallActivity::class.java).apply {
                putExtra(CallActivity.EXTRA_REMOTE_NUMBER, caller)
                putExtra(CallActivity.EXTRA_IS_OUTGOING, false)
            })
            finish()
        }

        binding.btnDecline.setOnClickListener {
            call?.let { SipManager.declineCall(it) }
            finish()
        }
    }

    override fun onRegistrationStateChanged(state: RegistrationState, message: String) {}

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        if (state == Call.State.End || state == Call.State.Released) {
            runOnUiThread { finish() }
        }
    }

    override fun onIncomingCall(call: Call) {}

    override fun onBackPressed() {
        // Block — user must accept or decline
    }

    override fun onDestroy() {
        SipManager.setListener(null)
        super.onDestroy()
    }
}
