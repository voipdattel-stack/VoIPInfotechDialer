package com.voipinfotech.dialer.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.voipinfotech.dialer.databinding.FragmentSettingsBinding
import com.voipinfotech.dialer.sip.SipManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedCredentials()

        binding.btnSave.setOnClickListener { saveAndRegister() }
        binding.btnUnregister.setOnClickListener { unregister() }

        updateStatus()
    }

    private fun loadSavedCredentials() {
        val prefs = requireContext().getSharedPreferences("sip_prefs", Context.MODE_PRIVATE)
        binding.etServer.setText(prefs.getString("server",   "sip.voipinfotech.com"))
        binding.etUsername.setText(prefs.getString("username", ""))
        binding.etPassword.setText(prefs.getString("password", ""))
    }

    private fun saveAndRegister() {
        val server   = binding.etServer.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (server.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        SipManager.register(requireContext(), server, username, password)
        Toast.makeText(requireContext(), "Registering…", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun unregister() {
        SipManager.unregister()
        Toast.makeText(requireContext(), "Unregistered", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun updateStatus() {
        val registered = SipManager.isRegistered()
        binding.tvStatus.text = if (registered) "● Registered" else "○ Not Registered"
        binding.tvStatus.setTextColor(
            requireContext().getColor(
                if (registered) com.voipinfotech.dialer.R.color.green
                else com.voipinfotech.dialer.R.color.red
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
