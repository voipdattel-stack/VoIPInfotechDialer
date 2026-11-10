package com.voipinfotech.dialer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.voipinfotech.dialer.databinding.FragmentDialerBinding
import com.voipinfotech.dialer.sip.SipManager

class DialerFragment : Fragment() {

    private var _binding: FragmentDialerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentDialerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val keys = listOf(
            binding.key0, binding.key1, binding.key2, binding.key3,
            binding.key4, binding.key5, binding.key6, binding.key7,
            binding.key8, binding.key9, binding.keyStar, binding.keyHash
        )
        val values = listOf("0","1","2","3","4","5","6","7","8","9","*","#")

        keys.forEachIndexed { i, btn ->
            btn.setOnClickListener { appendDigit(values[i]) }
        }

        binding.btnBackspace.setOnClickListener { deleteLastDigit() }
        binding.btnBackspace.setOnLongClickListener { clearDisplay(); true }
        binding.btnCall.setOnClickListener { initiateCall() }
    }

    private fun appendDigit(digit: String) {
        binding.tvNumber.append(digit)
    }

    private fun deleteLastDigit() {
        val text = binding.tvNumber.text
        if (text.isNotEmpty()) binding.tvNumber.text = text.dropLast(1)
    }

    private fun clearDisplay() {
        binding.tvNumber.text = ""
    }

    private fun initiateCall() {
        val number = binding.tvNumber.text.toString().trim()
        if (number.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a number to call", Toast.LENGTH_SHORT).show()
            return
        }
        if (!SipManager.isRegistered()) {
            Toast.makeText(requireContext(), "Not registered — check Settings", Toast.LENGTH_LONG).show()
            return
        }
        SipManager.makeCall(number)
        val intent = Intent(requireContext(), CallActivity::class.java).apply {
            putExtra(CallActivity.EXTRA_REMOTE_NUMBER, number)
            putExtra(CallActivity.EXTRA_IS_OUTGOING, true)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
