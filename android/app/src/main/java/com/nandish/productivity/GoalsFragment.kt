package com.nandish.productivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.nandish.productivity.databinding.FragmentGoalsBinding
import com.nandish.productivity.databinding.ItemGoalCardBinding
import java.util.Locale
import java.util.UUID

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNewQuest.setOnClickListener { showNewGoal() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val state = StateRepository.get()
        val today = SeedData.today()
        val done = state.habitLogs[today]?.values?.count { it == "true" } ?: 0
        val total = state.habits.size.coerceAtLeast(1)
        val pass = (done * 100 / total).coerceIn(0, 100)
        binding.textStreak.text = "42 DAY STREAK"
        binding.textPassRate.text = "$pass% WEEKLY PASS RATE"
        binding.progressWeekly.progress = pass
        binding.textActiveHabits.text = "ACTIVE HABITS · $done / $total completed"

        binding.goalsList.removeAllViews()
        state.goals.forEach { g ->
            val row = ItemGoalCardBinding.inflate(layoutInflater, binding.goalsList, false)
            row.textQuestTitle.text = "${g.target}: ${g.title.uppercase(Locale.US)}"
            row.progressQuest.progress = g.progress.coerceIn(0, 100)
            row.textQuestPct.text = "${g.progress}%"
            binding.goalsList.addView(row.root)
        }
    }

    private fun showNewGoal() {
        val input = EditText(requireContext()).apply { hint = "Quest title" }
        AlertDialog.Builder(requireContext())
            .setTitle("New quest")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    goals.add(
                        Goal(
                            id = "g_" + UUID.randomUUID().toString().replace("-", "").take(10),
                            title = title,
                            target = "QUEST",
                            deadline = "",
                            progress = 0
                        )
                    )
                }
                refresh()
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
