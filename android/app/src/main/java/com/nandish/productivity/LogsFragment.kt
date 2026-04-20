package com.nandish.productivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nandish.productivity.databinding.FragmentLogsBinding
import com.nandish.productivity.databinding.ItemSupplementLogBinding

class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun updateHeader() {
        val state = StateRepository.get()
        val today = SeedData.today()
        val log = state.supplementLogs[today] ?: HashMap()
        val total = state.supplements.size.coerceAtLeast(1)
        val taken = log.values.count { it }
        val pct = (taken * 100 / total).coerceIn(0, 100)
        binding.textCompletion.text = "$pct%"
        binding.progressSupplements.progress = pct
    }

    private fun refresh() {
        val state = StateRepository.get()
        val today = SeedData.today()
        val log = state.supplementLogs[today] ?: HashMap()
        updateHeader()

        fun refill(container: ViewGroup, time: String) {
            container.removeAllViews()
            state.supplements.filter { it.time == time }.forEach { s ->
                val row = ItemSupplementLogBinding.inflate(layoutInflater, container, false)
                row.textName.text = s.name
                row.textDose.text = s.dose
                row.checkTaken.setOnCheckedChangeListener(null)
                row.checkTaken.isChecked = log[s.id] == true
                row.checkTaken.setOnCheckedChangeListener { _, checked ->
                    StateRepository.update {
                        if (!supplementLogs.containsKey(today)) {
                            supplementLogs[today] = HashMap()
                        }
                        supplementLogs[today]!![s.id] = checked
                    }
                    updateHeader()
                }
                container.addView(row.root)
            }
        }

        refill(binding.listMorning, "morning")
        refill(binding.listWorkout, "workout")
        refill(binding.listNight, "night")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
