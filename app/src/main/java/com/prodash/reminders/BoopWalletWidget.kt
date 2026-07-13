package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class BoopWalletWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val accounts = repo.readAccounts()
        val entries = repo.readLedgerEntries()
        val balances = accounts.associate { it.id to it.openingBalance }.toMutableMap()
        entries.forEach { entry ->
            when (entry.type) {
                "income" -> balances[entry.accountId] = (balances[entry.accountId] ?: 0.0) + entry.amount
                "expense" -> balances[entry.accountId] = (balances[entry.accountId] ?: 0.0) - entry.amount
                "transfer" -> {
                    balances[entry.accountId] = (balances[entry.accountId] ?: 0.0) - entry.amount
                    entry.toAccountId?.let { toId -> balances[toId] = (balances[toId] ?: 0.0) + entry.amount }
                }
            }
        }
        val net = balances.values.sum()
        val pattern = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val title = if (accounts.isEmpty()) "No accounts yet" else pattern.format(net)
        val subtitle = if (accounts.isEmpty()) {
            "Tap to open wallet"
        } else {
            "${accounts.size} account${if (accounts.size == 1) "" else "s"} · net balance"
        }
        val clickIntent = BoopWidgetSupport.openTabIntent(context, "WALLET", requestCode = 7201)
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, clickIntent)
    }
}
