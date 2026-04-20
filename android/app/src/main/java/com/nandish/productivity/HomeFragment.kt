package com.nandish.productivity

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nandish.productivity.databinding.FragmentHomeBinding
import com.nandish.productivity.databinding.ItemFocusRowBinding
import com.nandish.productivity.databinding.ItemStackTileBinding
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDeposit.setOnClickListener {
            Toast.makeText(requireContext(), "Deposit", Toast.LENGTH_SHORT).show()
        }
        binding.buttonDetails.setOnClickListener {
            Toast.makeText(requireContext(), "Details", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val state = StateRepository.get()
        val today = SeedData.today()
        binding.textDate.text = formatDayHeader(today)
        binding.textWelcome.text = "Welcome,\n${greetingName()}."
        val total = state.accounts.sumOf { it.balance }
        val parts = formatMoney(total).removePrefix("-").split(".")
        val whole = parts.getOrElse(0) { "$0" }
        val cents = parts.getOrElse(1) { "00" }
        binding.textVaultAmount.text = if (total < 0) "-$whole.$cents" else "$whole.$cents"
        binding.textVaultTrend.text = "↑ +12.4% vs month"

        val done = state.tasks.count { it.done }
        val totalT = state.tasks.size.coerceAtLeast(1)
        binding.textFocusMeta.text = "$done of $totalT done"

        val habitsDone = state.habitLogs[today]?.values?.count { it == "true" } ?: 0
        val hPct = if (state.habits.isEmpty()) 82 else (habitsDone * 100 / state.habits.size.coerceAtLeast(1))
        binding.textCadencePct.text = "$hPct%"

        binding.focusList.removeAllViews()
        state.tasks.sortedWith(compareBy({ it.done }, { it.priorityWeight() })).take(5).forEach { task ->
            val row = ItemFocusRowBinding.inflate(layoutInflater, binding.focusList, false)
            row.textStatus.text = if (task.done) "✓" else "○"
            row.textTitle.text = task.title
            if (task.done) {
                row.textTitle.paintFlags = row.textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                row.textTitle.setTextColor(resources.getColor(R.color.outline, null))
            } else {
                row.textTitle.paintFlags = row.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                row.textTitle.setTextColor(resources.getColor(R.color.on_background, null))
            }
            row.root.setOnClickListener {
                StateRepository.update {
                    val t = tasks.find { it.id == task.id } ?: return@update
                    t.done = !t.done
                }
                refresh()
            }
            binding.focusList.addView(row.root)
        }

        binding.dailyStack.removeAllViews()
        val stack = state.supplements.filter { it.time == "morning" }.take(4)
        stack.forEachIndexed { i, s ->
            val tile = ItemStackTileBinding.inflate(layoutInflater, binding.dailyStack, false)
            tile.textDose.text = s.dose.uppercase(Locale.US)
            tile.textName.text = s.name
            tile.textTag.text = "Neuro-support"
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(i % 2, 1f)
                rowSpec = GridLayout.spec(i / 2, 1f)
                setMargins(8, 8, 8, 8)
            }
            binding.dailyStack.addView(tile.root, lp)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Task.priorityWeight(): Int = when (priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }
}
