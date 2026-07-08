package com.prodash.reminders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnifiedAppChrome(
    bottomNav: @Composable () -> Unit,
    overlay: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Box(
        Modifier
            .fillMaxSize()
            .background(palette.phoneBg),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
            ) {
                content()
            }
            bottomNav()
        }
        overlay()
    }
}

@Composable
fun UnifiedBottomNav(
    tabs: List<BoopTab>,
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    val half = (tabs.size + 1) / 2
    val leftTabs = tabs.subList(0, half)
    val rightTabs = tabs.subList(half, tabs.size)
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = palette.topbarBg,
            border = BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.14f)),
            shadowElevation = 6.dp,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leftTabs.forEachIndexed { i, tab ->
                    UnifiedNavIcon(tab = tab, selected = i == selectedIndex, onClick = { onSelectTab(i) }, modifier = Modifier.weight(1f))
                }
                UnifiedNavAddButton(onClick = onAdd)
                rightTabs.forEachIndexed { i, tab ->
                    val index = half + i
                    UnifiedNavIcon(tab = tab, selected = index == selectedIndex, onClick = { onSelectTab(index) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun UnifiedNavIcon(
    tab: BoopTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(stiffness = 420f, dampingRatio = 0.72f),
        label = "nav_icon_scale",
    )
    Box(
        modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = if (selected) palette.accent else palette.muted,
            modifier = Modifier
                .size(27.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
        )
    }
}

@Composable
private fun UnifiedNavAddButton(onClick: () -> Unit) {
    val palette = LocalBoopPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "add_scale",
    )
    val rotation by animateFloatAsState(
        targetValue = if (pressed) 45f else 0f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.7f),
        label = "add_rotation",
    )
    Box(
        Modifier.padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = palette.accent,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            interactionSource = interaction,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer { rotationZ = rotation },
                )
            }
        }
    }
}

data class UnifiedCreateOption(
    val label: String,
    val icon: ImageVector,
    val type: UnifiedItemType,
    val onClick: () -> Unit,
)

@Composable
fun UnifiedCreateSheet(
    open: Boolean,
    onDismiss: () -> Unit,
    options: List<UnifiedCreateOption>,
) {
    val palette = LocalBoopPalette.current
    AnimatedVisibility(
        visible = open,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(palette.overlay)
                    .clickable(onClick = onDismiss),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = palette.sheetBg,
                shadowElevation = 12.dp,
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 18.dp, bottom = 26.dp)
                        .navigationBarsPadding(),
                ) {
                    Text(
                        "Create",
                        color = palette.onBackground,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        "What do you want to add?",
                        color = palette.muted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                    )
                    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { option ->
                                    val colors = unifiedTypeColors(option.type, dark)
                                    Surface(
                                        onClick = {
                                            option.onClick()
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = colors.bg,
                                        border = BorderStroke(1.dp, colors.border),
                                    ) {
                                        Column(
                                            Modifier.padding(vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (dark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.55f),
                                                modifier = Modifier.size(42.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(option.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(21.dp))
                                                }
                                            }
                                            Text(
                                                option.label,
                                                color = palette.onBackground,
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                    }
                                }
                                repeat(3 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedSectionLabel(text: String, modifier: Modifier = Modifier) {
    val palette = LocalBoopPalette.current
    Text(
        text.uppercase(),
        modifier = modifier,
        color = palette.muted,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.5.sp,
            letterSpacing = 1.4.sp,
        ),
    )
}

@Composable
fun UnifiedFilterChips(
    chips: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { (id, label) ->
            val active = selected == id
            Surface(
                onClick = { onSelect(id) },
                shape = RoundedCornerShape(999.dp),
                color = if (active) palette.chipBg else palette.surface,
                border = BorderStroke(1.dp, if (active) palette.accent else palette.surfaceBorder),
            ) {
                Text(
                    label,
                    color = palette.onBackground,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
        }
    }
}

fun defaultCreateOptions(
    showHabits: Boolean,
    showWallet: Boolean,
    onNote: () -> Unit,
    onReminder: () -> Unit,
    onEvent: () -> Unit,
    onHabit: () -> Unit,
    onWallet: () -> Unit,
    onAccount: () -> Unit = {},
): List<UnifiedCreateOption> = buildList {
    add(UnifiedCreateOption("Note", Icons.Outlined.EditNote, UnifiedItemType.NOTE, onNote))
    add(UnifiedCreateOption("Reminder", Icons.Outlined.Notifications, UnifiedItemType.REMINDER, onReminder))
    add(UnifiedCreateOption("Event", Icons.Outlined.CalendarMonth, UnifiedItemType.CALENDAR, onEvent))
    if (showHabits) add(UnifiedCreateOption("Habit", Icons.Outlined.Flag, UnifiedItemType.HABIT, onHabit))
    if (showWallet) {
        add(UnifiedCreateOption("Expense", Icons.Outlined.AttachMoney, UnifiedItemType.WALLET, onWallet))
        add(UnifiedCreateOption("Account", Icons.Outlined.AccountBalance, UnifiedItemType.WALLET, onAccount))
    }
}
