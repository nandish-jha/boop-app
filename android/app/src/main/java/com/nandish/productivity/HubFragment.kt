package com.nandish.productivity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nandish.productivity.databinding.FragmentHubBinding
import java.util.Locale
import java.util.UUID

class HubFragment : Fragment() {

    private var _binding: FragmentHubBinding? = null
    private val binding get() = _binding!!

    private val adapter = StreamAdapter(
        onTaskToggle = { task ->
            StateRepository.update {
                val t = tasks.find { it.id == task.id } ?: return@update
                t.done = !t.done
            }
            applyList()
        },
        onTaskEdit = { task -> showEditTask(task) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerStream.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStream.adapter = adapter
        binding.buttonCreate.setOnClickListener { showNewTask() }
        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        applyList()
    }

    private fun applyList() {
        val state = StateRepository.get()
        val q = binding.inputSearch.text?.toString()?.lowercase(Locale.US).orEmpty()
        val taskItems = state.tasks
            .filter { it.title.lowercase(Locale.US).contains(q) }
            .sortedWith(compareBy({ it.done }, { it.priorityWeight() }))
            .map { StreamItem.TaskStream(it) }
        val noteItems = state.notes
            .filter {
                it.title.lowercase(Locale.US).contains(q) ||
                    it.body.lowercase(Locale.US).contains(q)
            }
            .sortedByDescending { it.updated }
            .map { StreamItem.NoteStream(it) }
        adapter.submitList(taskItems + noteItems)
    }

    private fun showNewTask() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("New task")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    tasks.add(
                        Task(
                            id = "t_" + UUID.randomUUID().toString().replace("-", "").take(10),
                            title = title,
                            done = false,
                            priority = "medium",
                            type = "personal",
                            due = SeedData.today()
                        )
                    )
                }
                applyList()
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showEditTask(task: Task) {
        val input = EditText(requireContext()).apply { setText(task.title) }
        AlertDialog.Builder(requireContext())
            .setTitle("Edit task")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    val t = tasks.find { it.id == task.id } ?: return@update
                    t.title = title
                }
                applyList()
            }
            .setNeutralButton("Delete") { _, _ ->
                StateRepository.update {
                    tasks.removeAll { it.id == task.id }
                }
                applyList()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
