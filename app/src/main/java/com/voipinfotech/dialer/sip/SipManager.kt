package com.voipinfotech.dialer.sip

import android.content.Context
import org.linphone.core.*

object SipManager {

    private var core: Core? = null
    private var sipListener: SipListener? = null

    interface SipListener {
        fun onRegistrationStateChanged(state: RegistrationState, message: String)
        fun onCallStateChanged(call: Call, state: Call.State, message: String)
        fun onIncomingCall(call: Call)
    }

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core, account: Account, state: RegistrationState, message: String
        ) {
            sipListener?.onRegistrationStateChanged(state, message)
        }

        override fun onCallStateChanged(
            core: Core, call: Call, state: Call.State, message: String
        ) {
            if (state == Call.State.IncomingReceived) {
                sipListener?.onIncomingCall(call)
            } else {
                sipListener?.onCallStateChanged(call, state, message)
            }
        }
    }

    fun init(context: Context) {
        if (core != null) return
        val factory = Factory.instance()
        factory.setDebugMode(false, "VoIPInfotech")
        val c = factory.createCore(null, null, context)
        c.addListener(coreListener)
        c.start()
        core = c

        val prefs = context.getSharedPreferences("sip_prefs", Context.MODE_PRIVATE)
        val server   = prefs.getString("server",   "") ?: ""
        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        if (server.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            register(context, server, username, password)
        }
    }

    fun register(context: Context, server: String, username: String, password: String, port: Int = 5060) {
        val c = core ?: return
        c.accountList.forEach { c.removeAccount(it) }
        c.clearAllAuthInfo()

        val factory = Factory.instance()
        val authInfo = factory.createAuthInfo(username, null, password, null, null, server)
        c.addAuthInfo(authInfo)

        val identity = factory.createAddress("sip:$username@$server") ?: return
        val serverAddr = factory.createAddress("sip:$server:$port") ?: return
        serverAddr.transport = TransportType.Udp

        val params = c.createAccountParams()
        params.identityAddress = identity
        params.serverAddress = serverAddr
        params.isRegisterEnabled = true
        params.expires = 3600

        val account = c.createAccount(params)
        c.addAccount(account)
        c.defaultAccount = account

        // Persist credentials
        context.getSharedPreferences("sip_prefs", Context.MODE_PRIVATE).edit()
            .putString("server",   server)
            .putString("username", username)
            .putString("password", password)
            .apply()
    }

    fun unregister() {
        val c = core ?: return
        c.defaultAccount?.let { account ->
            val p = account.params.clone()
            p.isRegisterEnabled = false
            account.params = p
        }
        c.accountList.forEach { c.removeAccount(it) }
        c.clearAllAuthInfo()
    }

    fun makeCall(destination: String): Call? {
        val c = core ?: return null
        val address = c.interpretUrl(destination) ?: return null
        val params  = c.createCallParams(null) ?: return null
        params.enableVideo(false)
        return c.inviteAddressWithParams(address, params)
    }

    fun answerCall(call: Call) {
        val params = core?.createCallParams(call)
        params?.enableVideo(false)
        if (params != null) call.acceptWithParams(params)
        else call.accept()
    }

    fun declineCall(call: Call) {
        call.decline(Reason.Declined)
    }

    fun hangup() {
        core?.currentCall?.terminate()
    }

    fun toggleMute(): Boolean {
        val call = core?.currentCall ?: return false
        call.microphoneMuted = !call.microphoneMuted
        return call.microphoneMuted
    }

    fun isMuted(): Boolean = core?.currentCall?.microphoneMuted ?: false

    fun toggleSpeaker(): Boolean {
        val c = core ?: return false
        val isSpeaker = c.outputAudioDevice?.type == AudioDevice.Type.Speaker
        val target = if (isSpeaker) {
            c.audioDevices.firstOrNull { it.type == AudioDevice.Type.Earpiece }
        } else {
            c.audioDevices.firstOrNull { it.type == AudioDevice.Type.Speaker }
        }
        c.outputAudioDevice = target
        return !isSpeaker
    }

    fun isSpeaker(): Boolean = core?.outputAudioDevice?.type == AudioDevice.Type.Speaker

    fun getCurrentCall(): Call? = core?.currentCall

    fun getCallLogs(): Array<CallLog> = core?.callLogs ?: emptyArray()

    fun isRegistered(): Boolean =
        core?.defaultAccount?.state == RegistrationState.Ok

    fun setListener(l: SipListener?) { sipListener = l }

    fun destroy() {
        core?.stop()
        core?.removeListener(coreListener)
        core = null
    }
}
