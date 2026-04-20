package com.nandish.productivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.nandish.productivity.databinding.FragmentVaultBinding
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
        binding.linkLedger.setOnClickListener {
            Toast.makeText(requireContext(), "Ledger", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val state = StateRepository.get()
        val total = state.accounts.sumOf { it.balance }
        binding.textNetWorth.text = formatMoney(total)
        val monthIncome = state.transactions.filter { it.type == "income" }.sumOf { it.amount }
        val monthExp = state.transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val delta = monthIncome - monthExp
        binding.textDelta.text = "MONTHLY DELTA ${if (delta >= 0) "+" else ""}${formatMoney(delta)}"

        binding.accountsList.removeAllViews()
        state.accounts.forEach { a ->
            val card = MaterialCardView(requireContext()).apply {
                setCardBackgroundColor(resources.getColor(R.color.surface_low, null))
                strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_width)
                strokeColor = resources.getColor(R.color.outline_variant, null)
                radius = resources.getDimension(R.dimen.card_corner)
                val p = (16 * resources.displayMetrics.density).toInt()
                setContentPadding(p, p, p, p)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (10 * resources.displayMetrics.density).toInt()
                }
            }
            val col = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
            }
            val title = com.google.android.material.textview.MaterialTextView(requireContext()).apply {
                text = a.name.uppercase(Locale.US)
                setTextColor(resources.getColor(R.color.primary, null))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val sub = com.google.android.material.textview.MaterialTextView(requireContext()).apply {
                text = "${a.bank} · ${a.type}"
                setTextColor(resources.getColor(R.color.on_surface_variant, null))
                textSize = 12f
            }
            val amt = com.google.android.material.textview.MaterialTextView(requireContext()).apply {
                text = formatMoney(a.balance)
                setTextColor(resources.getColor(R.color.primary, null))
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            col.addView(title)
            col.addView(sub)
            col.addView(amt)
            card.addView(col)
            binding.accountsList.addView(card)
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
                val row = com.google.android.material.textview.MaterialTextView(requireContext()).apply {
                    text = "${tx.category} · ${tx.date} · ${formatMoney(tx.amount)}"
                    setTextColor(resources.getColor(R.color.on_background, null))
                    textSize = 14f
                    val p = (12 * resources.displayMetrics.density).toInt()
                    setPadding(0, p, 0, p)
                }
                binding.txnList.addView(row)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
