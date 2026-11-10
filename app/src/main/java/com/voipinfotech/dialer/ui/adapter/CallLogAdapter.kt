package com.voipinfotech.dialer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voipinfotech.dialer.R
import com.voipinfotech.dialer.databinding.ItemCallLogBinding
import org.linphone.core.Call
import org.linphone.core.CallLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallLogAdapter(
    private val logs: List<CallLog>,
    private val onCallBack: (CallLog) -> Unit
) : RecyclerView.Adapter<CallLogAdapter.VH>() {

    inner class VH(val binding: ItemCallLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val log = logs[position]
        val b = holder.binding
        val remote = log.remoteAddress?.username ?: "Unknown"
        b.tvName.text = remote
        b.tvDate.text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            .format(Date(log.startDate * 1000L))

        val (iconRes, dirLabel) = when (log.dir) {
            Call.Dir.Incoming -> when {
                log.status == org.linphone.core.Call.Status.Missed ->
                    Pair(R.drawable.ic_call_missed, "Missed")
                else ->
                    Pair(R.drawable.ic_call_incoming, "Incoming")
            }
            else -> Pair(R.drawable.ic_call_outgoing, "Outgoing")
        }
        b.ivDirection.setImageResource(iconRes)
        b.tvDirection.text = dirLabel

        val dur = log.duration
        b.tvDuration.text = if (dur > 0) "%d:%02d".format(dur / 60, dur % 60) else ""

        b.btnCallback.setOnClickListener { onCallBack(log) }
    }

    override fun getItemCount() = logs.size
}
