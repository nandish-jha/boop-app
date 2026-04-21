package com.nandish.productivity

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

object ItemEditors {

    private fun newId(): String =
        UUID.randomUUID().toString().replace("-", "").take(12)

    private fun dp(f: Fragment, d: Int): Int =
        (f.resources.displayMetrics.density * d).toInt()

    private fun edit(f: Fragment, hint: String, def: String = ""): EditText =
        EditText(f.requireContext()).apply {
            this.hint = hint
            setText(def)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(f, 20), dp(f, 8), dp(f, 20), dp(f, 8))
        }

    private fun scrollWrap(f: Fragment, vararg views: View): ScrollView {
        val pad = dp(f, 8)
        val col = LinearLayout(f.requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            views.forEach { addView(it) }
        }
        return ScrollView(f.requireContext()).apply {
            addView(col)
        }
    }

    fun showTaskEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().tasks.find { t -> t.id == it } }
        val titleEt = edit(f, "Title", snap?.title ?: "")
        val dueEt = edit(f, "Due (yyyy-MM-dd, optional)", snap?.due ?: "")
        val priSpinner = spinner(f, arrayOf("high", "medium", "low"), snap?.priority ?: "medium")
        val typeSpinner = spinner(f, arrayOf("work", "personal", "errand"), snap?.type ?: "personal")
        val scroll = scrollWrap(f, titleEt, dueEt, priSpinner, typeSpinner)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New task" else "Edit task")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val title = titleEt.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        tasks.add(
                            Task(
                                id = newId(),
                                title = title,
                                done = false,
                                priority = priSpinner.selectedItem as String,
                                type = typeSpinner.selectedItem as String,
                                due = dueEt.text?.toString()?.trim().orEmpty()
                            )
                        )
                    } else {
                        val t = tasks.find { it.id == editId } ?: return@update
                        t.title = title
                        t.due = dueEt.text?.toString()?.trim().orEmpty()
                        t.priority = priSpinner.selectedItem as String
                        t.type = typeSpinner.selectedItem as String
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update { tasks.removeAll { it.id == editId } }
                onDone()
            }
        }
        builder.show()
    }

    fun showNoteEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().notes.find { n -> n.id == it } }
        val titleEt = edit(f, "Title", snap?.title ?: "")
        val bodyEt = edit(f, "Body", snap?.body ?: "").apply {
            minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        val tagEt = edit(f, "Tag", snap?.tags?.firstOrNull() ?: "")
        val scroll = scrollWrap(f, titleEt, bodyEt, tagEt)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New note" else "Edit note")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val title = titleEt.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        notes.add(
                            Note(
                                id = newId(),
                                title = title,
                                body = bodyEt.text?.toString()?.trim().orEmpty(),
                                tags = arrayListOf(tagEt.text?.toString()?.trim().orEmpty().ifBlank { "Note" }),
                                updated = System.currentTimeMillis()
                            )
                        )
                    } else {
                        val n = notes.find { it.id == editId } ?: return@update
                        n.title = title
                        n.body = bodyEt.text?.toString()?.trim().orEmpty()
                        n.tags.clear()
                        n.tags.add(tagEt.text?.toString()?.trim().orEmpty().ifBlank { "Note" })
                        n.updated = System.currentTimeMillis()
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update { notes.removeAll { it.id == editId } }
                onDone()
            }
        }
        builder.show()
    }

    fun showGoalEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().goals.find { g -> g.id == it } }
        val titleEt = edit(f, "Title", snap?.title ?: "")
        val targetEt = edit(f, "Target label", snap?.target ?: "")
        val deadlineEt = edit(f, "Deadline (optional)", snap?.deadline ?: "")
        val progEt = edit(f, "Progress 0–100", snap?.progress?.toString() ?: "0").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val scroll = scrollWrap(f, titleEt, targetEt, deadlineEt, progEt)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New goal" else "Edit goal")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val title = titleEt.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) return@setPositiveButton
                val prog = progEt.text?.toString()?.trim()?.toIntOrNull()?.coerceIn(0, 100) ?: 0
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        goals.add(
                            Goal(
                                id = newId(),
                                title = title,
                                target = targetEt.text?.toString()?.trim().orEmpty(),
                                deadline = deadlineEt.text?.toString()?.trim().orEmpty(),
                                progress = prog
                            )
                        )
                    } else {
                        val g = goals.find { it.id == editId } ?: return@update
                        g.title = title
                        g.target = targetEt.text?.toString()?.trim().orEmpty()
                        g.deadline = deadlineEt.text?.toString()?.trim().orEmpty()
                        g.progress = prog
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update { goals.removeAll { it.id == editId } }
                onDone()
            }
        }
        builder.show()
    }

    fun showHabitEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().habits.find { h -> h.id == it } }
        val nameEt = edit(f, "Habit name", snap?.name ?: "")
        val unitEt = edit(f, "Unit (e.g. min, ml)", snap?.unit ?: "")
        val scroll = scrollWrap(f, nameEt, unitEt)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New habit" else "Edit habit")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = nameEt.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        habits.add(
                            Habit(
                                id = newId(),
                                name = name,
                                unit = unitEt.text?.toString()?.trim().orEmpty(),
                                type = "check",
                                target = 1
                            )
                        )
                    } else {
                        val h = habits.find { it.id == editId } ?: return@update
                        h.name = name
                        h.unit = unitEt.text?.toString()?.trim().orEmpty()
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update {
                    habits.removeAll { it.id == editId }
                    habitLogs.values.forEach { m -> m.remove(editId) }
                }
                onDone()
            }
        }
        builder.show()
    }

    fun showSupplementEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().supplements.find { s -> s.id == it } }
        val nameEt = edit(f, "Name", snap?.name ?: "")
        val doseEt = edit(f, "Dose", snap?.dose ?: "")
        val timeSpinner = spinner(f, arrayOf("morning", "evening", "night"), snap?.time ?: "morning")
        val scroll = scrollWrap(f, nameEt, doseEt, timeSpinner)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New supplement" else "Edit supplement")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = nameEt.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) return@setPositiveButton
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        supplements.add(
                            Supplement(
                                id = newId(),
                                name = name,
                                dose = doseEt.text?.toString()?.trim().orEmpty(),
                                time = timeSpinner.selectedItem as String
                            )
                        )
                    } else {
                        val s = supplements.find { it.id == editId } ?: return@update
                        s.name = name
                        s.dose = doseEt.text?.toString()?.trim().orEmpty()
                        s.time = timeSpinner.selectedItem as String
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update {
                    supplements.removeAll { it.id == editId }
                    supplementLogs.values.forEach { m -> m.remove(editId) }
                }
                onDone()
            }
        }
        builder.show()
    }

    fun showAccountEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val snap = id?.let { StateRepository.get().accounts.find { a -> a.id == it } }
        val nameEt = edit(f, "Account name", snap?.name ?: "")
        val bankEt = edit(f, "Institution", snap?.bank ?: "")
        val typeSpinner = spinner(
            f,
            arrayOf("chequing", "savings", "tfsa", "credit"),
            (snap?.type ?: "chequing").lowercase()
        )
        val balEt = edit(f, "Balance", snap?.balance?.toString() ?: "0").apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        val scroll = scrollWrap(f, nameEt, bankEt, typeSpinner, balEt)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New account" else "Edit account")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = nameEt.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) return@setPositiveButton
                val bal = balEt.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        accounts.add(
                            Account(
                                id = newId(),
                                name = name,
                                bank = bankEt.text?.toString()?.trim().orEmpty(),
                                type = typeSpinner.selectedItem as String,
                                balance = bal
                            )
                        )
                    } else {
                        val a = accounts.find { it.id == editId } ?: return@update
                        a.name = name
                        a.bank = bankEt.text?.toString()?.trim().orEmpty()
                        a.type = typeSpinner.selectedItem as String
                        a.balance = bal
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update {
                    accounts.removeAll { it.id == editId }
                    transactions.removeAll { it.accountId == editId }
                }
                onDone()
            }
        }
        builder.show()
    }

    fun showTransactionEditor(f: Fragment, id: String?, onDone: () -> Unit) {
        val s0 = StateRepository.get()
        if (s0.accounts.isEmpty()) {
            Toast.makeText(f.requireContext(), "Add an account first.", Toast.LENGTH_SHORT).show()
            return
        }
        val snap = id?.let { s0.transactions.find { t -> t.id == it } }
        val typeSpinner = spinner(f, arrayOf("expense", "income"), snap?.type ?: "expense")
        val amtEt = edit(f, "Amount", snap?.amount?.let { kotlin.math.abs(it).toString() } ?: "").apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val catEt = edit(f, "Category", snap?.category ?: "")
        val noteEt = edit(f, "Note / payee", snap?.note ?: "")
        val dateEt = edit(f, "Date (yyyy-MM-dd)", snap?.date ?: SeedData.today())
        val accLabels = s0.accounts.map { "${it.name} (${it.bank})" }.toTypedArray()
        val accIds = s0.accounts.map { it.id }.toTypedArray()
        val accSpinner = AppCompatSpinner(f.requireContext()).apply {
            adapter = ArrayAdapter(f.requireContext(), android.R.layout.simple_spinner_dropdown_item, accLabels)
            val sel = snap?.accountId?.let { aid -> accIds.indexOf(aid).takeIf { it >= 0 } } ?: 0
            setSelection(sel.coerceAtLeast(0))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(f, 20), dp(f, 8), dp(f, 20), dp(f, 8))
        }
        val scroll = scrollWrap(f, typeSpinner, amtEt, catEt, noteEt, dateEt, accSpinner)
        val editId = id
        val builder = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(if (snap == null) "New transaction" else "Edit transaction")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val amt = amtEt.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
                val cat = catEt.text?.toString()?.trim().orEmpty().ifBlank { "General" }
                val accountId = accIds.getOrNull(accSpinner.selectedItemPosition) ?: return@setPositiveButton
                val dRaw = dateEt.text?.toString()?.trim()
                val dateStr = if (dRaw.isNullOrEmpty()) SeedData.today() else dRaw
                StateRepository.update {
                    if (editId.isNullOrBlank()) {
                        transactions.add(
                            Transaction(
                                id = newId(),
                                type = typeSpinner.selectedItem as String,
                                amount = amt,
                                category = cat,
                                accountId = accountId,
                                date = dateStr,
                                note = noteEt.text?.toString()?.trim().orEmpty()
                            )
                        )
                    } else {
                        val t = transactions.find { it.id == editId } ?: return@update
                        t.type = typeSpinner.selectedItem as String
                        t.amount = amt
                        t.category = cat
                        t.accountId = accountId
                        t.date = dateStr
                        t.note = noteEt.text?.toString()?.trim().orEmpty()
                    }
                }
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (!editId.isNullOrBlank()) {
            builder.setNeutralButton("Delete") { _, _ ->
                StateRepository.update { transactions.removeAll { it.id == editId } }
                onDone()
            }
        }
        builder.show()
    }

    fun showReminderEditor(f: Fragment, onDone: () -> Unit) {
        val snap = StateRepository.get().settings
        val et = edit(f, "Time (HH:mm)", snap.reminderTime)
        val scroll = scrollWrap(f, et)
        MaterialAlertDialogBuilder(f.requireContext())
            .setTitle("Daily reminder")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val v = et.text?.toString()?.trim().orEmpty().ifBlank { snap.reminderTime }
                StateRepository.update { settings.reminderTime = v }
                ReminderScheduler.schedule(f.requireContext().applicationContext)
                onDone()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showAddPicker(f: Fragment, onDone: () -> Unit) {
        MaterialAlertDialogBuilder(f.requireContext())
            .setTitle("Add")
            .setItems(
                arrayOf("Task", "Note", "Goal", "Habit", "Supplement", "Account", "Transaction")
            ) { _, which ->
                when (which) {
                    0 -> showTaskEditor(f, null, onDone)
                    1 -> showNoteEditor(f, null, onDone)
                    2 -> showGoalEditor(f, null, onDone)
                    3 -> showHabitEditor(f, null, onDone)
                    4 -> showSupplementEditor(f, null, onDone)
                    5 -> showAccountEditor(f, null, onDone)
                    6 -> showTransactionEditor(f, null, onDone)
                }
            }
            .show()
    }

    private fun spinner(f: Fragment, items: Array<String>, selected: String): AppCompatSpinner {
        val sp = AppCompatSpinner(f.requireContext())
        sp.adapter = ArrayAdapter(f.requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        val idx = items.indexOf(selected).takeIf { it >= 0 } ?: 0
        sp.setSelection(idx)
        sp.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        sp.setPadding(dp(f, 20), dp(f, 8), dp(f, 20), dp(f, 8))
        return sp
    }
}
