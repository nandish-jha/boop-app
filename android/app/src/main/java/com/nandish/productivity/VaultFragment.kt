package com.nandish.productivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nandish.productivity.databinding.FragmentVaultBinding
import com.nandish.productivity.databinding.ItemVaultAccountBinding
import com.nandish.productivity.databinding.ItemVaultTransactionBinding
import java.util.Locale

class VaultFragment : Fragment() {

    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDeposit.setOnClickListener {
            Toast.makeText(requireContext(), "Deposit", Toast.LENGTH_SHORT).show()
        }
        binding.buttonInvest.setOnClickListener {
            Toast.makeText(requireContext(), "Invest", Toast.LENGTH_SHORT).show()
        }
        binding.buttonAddAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Add account", Toast.LENGTH_SHORT).show()
        }
        binding.linkLedger.setOnClickListener {
            Toast.makeText(requireContext(), "Ledger", Toast.LENGTH_SHORT).show()
        }
        binding.buttonLogTransaction.setOnClickListener {
            Toast.makeText(requireContext(), "Transaction logged", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val state = StateRepository.get()
        val total = state.accounts.sumOf { it.balance }
        val formatted = formatMoney(total)
        val neg = formatted.startsWith("-")
        val body = formatted.removePrefix("-").removePrefix("$")
        val parts = body.split(".")
        binding.textNetWhole.text = (if (neg) "-$" else "$") + parts[0]
        binding.textNetCents.text = "." + parts.getOrElse(1) { "00" }

        val monthIncome = state.transactions.filter { it.type == "income" }.sumOf { it.amount }
        val monthExp = state.transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val delta = monthIncome - monthExp
        val trendPct =
            if (monthExp > 0) kotlin.math.abs((delta / monthExp) * 100).coerceAtMost(99.0) else 2.4
        binding.textMonthlyTrend.text =
            "${if (delta >= 0) "+" else "−"} ${String.format(Locale.US, "%.1f", trendPct)}% THIS MONTH"

        binding.textUpdated.text = "Updated 4 minutes ago"

        binding.accountsList.removeAllViews()
        state.accounts.forEach { a ->
            val row = ItemVaultAccountBinding.inflate(layoutInflater, binding.accountsList, false)
            row.textAccountTitle.text = accountTitle(a)
            row.textAccountSub.text = "${a.bank} · ${a.type}"
            row.textAccountBalance.text = formatMoney(a.balance)
            row.accentLeft.visibility = View.GONE
            row.textTag.visibility = View.GONE
            when (a.type.lowercase(Locale.US)) {
                "chequing" -> {
                    row.textTag.visibility = View.VISIBLE
                    row.textTag.text = "PRIMARY"
                    row.textTag.setTextColor(resources.getColor(R.color.primary, null))
                }
                "tfsa" -> {
                    row.textTag.visibility = View.VISIBLE
                    row.textTag.text = "ACTIVE GROWTH"
                    row.textTag.setTextColor(resources.getColor(R.color.on_surface_variant, null))
                    row.accentLeft.visibility = View.VISIBLE
                }
                "credit" -> {
                    row.textTag.visibility = View.VISIBLE
                    row.textTag.text = "DUE IN 4D"
                    row.textTag.setTextColor(resources.getColor(R.color.accent_alert, null))
                }
            }
            binding.accountsList.addView(row.root)
        }

        binding.txnList.removeAllViews()
        val recent = state.transactions.sortedByDescending { it.date }.take(6)
        if (recent.isEmpty()) {
            val empty = com.google.android.material.textview.MaterialTextView(requireContext()).apply {
                text = "No transactions yet."
                setTextColor(resources.getColor(R.color.outline, null))
            }
            binding.txnList.addView(empty)
        } else {
            recent.forEach { tx ->
                val row = ItemVaultTransactionBinding.inflate(layoutInflater, binding.txnList, false)
                val title = tx.note.ifBlank { tx.category }
                row.textTxnTitle.text = title
                row.textTxnMeta.text =
                    "${tx.category.uppercase(Locale.US)} · ${tx.date}"
                row.textTxnAmount.text = formatMoney(
                    if (tx.type == "expense") -kotlin.math.abs(tx.amount) else tx.amount
                )
                val acc = state.accounts.find { it.id == tx.accountId }
                row.textTxnAccount.text = acc?.let { accountTitle(it) } ?: "ACCOUNT"
                row.textTxnIcon.text = when (tx.category.lowercase(Locale.US)) {
                    "food" -> "⌁"
                    "electronics" -> "▢"
                    else -> "◆"
                }
                binding.txnList.addView(row.root)
            }
        }
    }

    private fun accountTitle(a: Account): String =
        when (a.type.lowercase(Locale.US)) {
            "chequing" -> "CHECKING"
            "savings" -> "SAVINGS"
            "tfsa" -> "INVESTMENT"
            "credit" -> "CREDIT CARD"
            else -> a.name.uppercase(Locale.US)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
