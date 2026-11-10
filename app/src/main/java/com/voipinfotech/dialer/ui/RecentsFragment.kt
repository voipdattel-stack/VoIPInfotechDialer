package com.voipinfotech.dialer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.voipinfotech.dialer.databinding.FragmentRecentsBinding
import com.voipinfotech.dialer.sip.SipManager
import com.voipinfotech.dialer.ui.adapter.CallLogAdapter

class RecentsFragment : Fragment() {

    private var _binding: FragmentRecentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentRecentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logs = SipManager.getCallLogs().toList()
        val adapter = CallLogAdapter(logs) { log ->
            val number = log.remoteAddress?.username ?: return@CallLogAdapter
            (parentFragment?.activity as? MainActivity)?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(com.voipinfotech.dialer.R.id.fragment_container, DialerFragment())
                    .commit()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.tvEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
