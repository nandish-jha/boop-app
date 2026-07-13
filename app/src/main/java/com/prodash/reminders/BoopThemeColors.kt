package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor

/** Resolves app palette colors for non-Compose surfaces (widgets). */
object BoopThemeColors {
    data class WidgetColors(
        val background: Int,
        val surface: Int,
        val title: Int,
        val subtitle: Int,
        val accent: Int,
    )

    fun resolve(context: Context): WidgetColors {
        LocalStore.init(context)
        val family = LocalStore.readPaletteFamily()
        val mode = LocalStore.readThemeMode()
        val systemDark = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val dark = when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> systemDark
        }
        return when (family) {
            PaletteFamily.AMOLED -> if (dark) amoledDark() else amoledLight()
            PaletteFamily.TERRACOTTA -> if (dark) terracottaDark() else terracottaLight()
            PaletteFamily.ROSE -> if (dark) roseDark() else roseLight()
            PaletteFamily.SLATE -> if (dark) slateDark() else slateLight()
            PaletteFamily.FOREST -> if (dark) forestDark() else forestLight()
            PaletteFamily.OCEAN -> if (dark) oceanDark() else oceanLight()
        }
    }

    fun refreshAllWidgets(context: Context) {
        val appCtx = context.applicationContext
        val manager = AppWidgetManager.getInstance(appCtx)
        val providers = listOf(
            BoopTasksWidget::class.java,
            BoopNotesWidget::class.java,
            BoopHabitsWidget::class.java,
            BoopUpNextWidget::class.java,
            BoopWalletWidget::class.java,
            BoopOverdueWidget::class.java,
            BoopStreakWidget::class.java,
            BoopTodaySummaryWidget::class.java,
        )
        providers.forEach { cls ->
            val ids = manager.getAppWidgetIds(ComponentName(appCtx, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(appCtx, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                appCtx.sendBroadcast(intent)
            }
        }
    }

    private fun amoledDark() = WidgetColors(
        background = AndroidColor.parseColor("#000000"),
        surface = AndroidColor.parseColor("#1A1A1A"),
        title = AndroidColor.parseColor("#FFFFFF"),
        subtitle = AndroidColor.parseColor("#9E9E9E"),
        accent = AndroidColor.parseColor("#E0E0E0"),
    )

    private fun amoledLight() = WidgetColors(
        background = AndroidColor.parseColor("#FFFFFF"),
        surface = AndroidColor.parseColor("#EDEDED"),
        title = AndroidColor.parseColor("#121212"),
        subtitle = AndroidColor.parseColor("#757575"),
        accent = AndroidColor.parseColor("#3A3A3A"),
    )

    private fun terracottaDark() = WidgetColors(
        background = AndroidColor.parseColor("#1A1918"),
        surface = AndroidColor.parseColor("#30302E"),
        title = AndroidColor.parseColor("#FAF9F5"),
        subtitle = AndroidColor.parseColor("#B0AEA5"),
        accent = AndroidColor.parseColor("#E88868"),
    )

    private fun terracottaLight() = WidgetColors(
        background = AndroidColor.parseColor("#FFFDF8"),
        surface = AndroidColor.parseColor("#F5F4ED"),
        title = AndroidColor.parseColor("#141413"),
        subtitle = AndroidColor.parseColor("#87867F"),
        accent = AndroidColor.parseColor("#D46E48"),
    )

    private fun roseDark() = WidgetColors(
        background = AndroidColor.parseColor("#1A1518"),
        surface = AndroidColor.parseColor("#2A2226"),
        title = AndroidColor.parseColor("#FFF7F8"),
        subtitle = AndroidColor.parseColor("#B5A4A8"),
        accent = AndroidColor.parseColor("#E08A9A"),
    )

    private fun roseLight() = WidgetColors(
        background = AndroidColor.parseColor("#FFFBFC"),
        surface = AndroidColor.parseColor("#F8EEEF"),
        title = AndroidColor.parseColor("#1A1214"),
        subtitle = AndroidColor.parseColor("#8A757A"),
        accent = AndroidColor.parseColor("#C45A6E"),
    )

    private fun slateDark() = WidgetColors(
        background = AndroidColor.parseColor("#14171C"),
        surface = AndroidColor.parseColor("#22262C"),
        title = AndroidColor.parseColor("#E8ECF0"),
        subtitle = AndroidColor.parseColor("#9AA3AD"),
        accent = AndroidColor.parseColor("#8FA8C4"),
    )

    private fun slateLight() = WidgetColors(
        background = AndroidColor.parseColor("#F8FAFC"),
        surface = AndroidColor.parseColor("#ECEFF3"),
        title = AndroidColor.parseColor("#14181E"),
        subtitle = AndroidColor.parseColor("#6F7884"),
        accent = AndroidColor.parseColor("#4F6F8F"),
    )

    private fun forestDark() = WidgetColors(
        background = AndroidColor.parseColor("#141A15"),
        surface = AndroidColor.parseColor("#222A23"),
        title = AndroidColor.parseColor("#EFF5EF"),
        subtitle = AndroidColor.parseColor("#9AAB9C"),
        accent = AndroidColor.parseColor("#7DB88A"),
    )

    private fun forestLight() = WidgetColors(
        background = AndroidColor.parseColor("#F8FBF8"),
        surface = AndroidColor.parseColor("#E8EFE8"),
        title = AndroidColor.parseColor("#121814"),
        subtitle = AndroidColor.parseColor("#6E7A6F"),
        accent = AndroidColor.parseColor("#3F7A4E"),
    )

    private fun oceanDark() = WidgetColors(
        background = AndroidColor.parseColor("#101820"),
        surface = AndroidColor.parseColor("#1C2730"),
        title = AndroidColor.parseColor("#ECF4FA"),
        subtitle = AndroidColor.parseColor("#95A8B6"),
        accent = AndroidColor.parseColor("#5EB0D4"),
    )

    private fun oceanLight() = WidgetColors(
        background = AndroidColor.parseColor("#F7FBFD"),
        surface = AndroidColor.parseColor("#E4EEF4"),
        title = AndroidColor.parseColor("#101820"),
        subtitle = AndroidColor.parseColor("#667A88"),
        accent = AndroidColor.parseColor("#2E7FA8"),
    )
}
