package com.nandish.productivity

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nandish.productivity.databinding.ItemStreamRowBinding
import java.util.Locale

sealed class StreamItem {
    data class TaskStream(val task: Task) : StreamItem()
    data class NoteStream(val note: Note) : StreamItem()
}

class StreamAdapter(
    private val onTaskToggle: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit
) : ListAdapter<StreamItem, StreamAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStreamRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemStreamRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StreamItem) {
            when (item) {
                is StreamItem.TaskStream -> {
                    val t = item.task
                    binding.textBadge.text = if (t.done) "COMPLETED" else t.priority.uppercase(Locale.US) + " PRIORITY"
                    binding.textTitle.text = t.title
                    binding.textTitle.paintFlags = if (t.done) {
                        binding.textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        binding.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    val dueLabel = if (t.due.isBlank()) "today" else t.due
                    binding.textSub.text = "${t.type.uppercase(Locale.US)} · due $dueLabel"
                    binding.root.setOnClickListener { onTaskEdit(t) }
                    binding.root.setOnLongClickListener {
                        onTaskToggle(t)
                        true
                    }
                }
                is StreamItem.NoteStream -> {
                    val n = item.note
                    binding.textBadge.text = "NOTE"
                    binding.textTitle.text = n.title
                    binding.textTitle.paintFlags = binding.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    val tags = n.tags.joinToString(" · ")
                    val whenStr = noteAge(n.updated)
                    binding.textSub.text = "$whenStr · $tags"
                    binding.root.setOnClickListener { /* future: note editor */ }
                }
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<StreamItem>() {
            override fun areItemsTheSame(a: StreamItem, b: StreamItem): Boolean {
                return when {
                    a is StreamItem.TaskStream && b is StreamItem.TaskStream -> a.task.id == b.task.id
                    a is StreamItem.NoteStream && b is StreamItem.NoteStream -> a.note.id == b.note.id
                    else -> false
                }
            }

            override fun areContentsTheSame(a: StreamItem, b: StreamItem): Boolean = a == b
        }
    }
}

private fun noteAge(updated: Long): String {
    val days = ((System.currentTimeMillis() - updated) / (86400000L)).toInt()
    return when {
        days <= 0 -> "Today"
        days == 1 -> "Yesterday"
        else -> "$days days ago"
    }
}
