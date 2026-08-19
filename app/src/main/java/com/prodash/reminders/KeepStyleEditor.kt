package com.prodash.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KeepSentenceKeyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)

@Composable
fun KeepToolbarIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = palette.chipBg,
        border = BorderStroke(1.dp, palette.surfaceBorder),
        modifier = modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tint ?: palette.onBackground,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun KeepEditorTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: @Composable () -> Unit = {},
) {
    val palette = LocalBoopPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(999.dp),
            color = palette.chipBg,
            border = BorderStroke(1.dp, palette.surfaceBorder),
        ) {
            Row(
                Modifier.padding(start = 10.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.onBackground,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Back",
                    color = palette.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    title,
                    color = palette.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BoopSerifFamily,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            actions()
        }
    }
}

@Composable
fun KeepOutlinedTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Title",
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textStyle = TextStyle(
            fontFamily = BoopSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = palette.onBackground,
        ),
        placeholder = {
            Text(
                placeholder,
                style = TextStyle(
                    fontFamily = BoopSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp,
                    color = palette.muted.copy(alpha = 0.65f),
                ),
            )
        },
        singleLine = true,
        keyboardOptions = KeepSentenceKeyboard,
        shape = RoundedCornerShape(14.dp),
        colors = KeepOutlinedFieldColors(),
    )
}

@Composable
fun KeepOutlinedBodyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Write something...",
    modifier: Modifier = Modifier,
    minLines: Int = 5,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text(placeholder) },
        minLines = minLines,
        keyboardOptions = KeepSentenceKeyboard,
        shape = RoundedCornerShape(14.dp),
        colors = KeepOutlinedFieldColors(),
    )
}

@Composable
fun KeepOutlinedBodyField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    placeholder: String = "Write something...",
    modifier: Modifier = Modifier,
    minLines: Int = 5,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text(placeholder) },
        minLines = minLines,
        keyboardOptions = KeepSentenceKeyboard,
        shape = RoundedCornerShape(14.dp),
        colors = KeepOutlinedFieldColors(),
        visualTransformation = visualTransformation,
    )
}

@Composable
fun KeepFormSaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "SAVE",
    enabled: Boolean = true,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) palette.accent else palette.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                color = if (enabled) palette.accentOn else palette.muted,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeepReminderLinkSection(
    options: List<Pair<String?, String>>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        KeepSectionLabel("LINK TO REMINDER")
        FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (id, label) ->
                KeepPaletteChip(
                    label = label,
                    selected = selectedId == id,
                    onClick = { onSelect(id) },
                )
            }
        }
    }
}

@Composable
fun KeepBorderlessTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    titleDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
) {
    val palette = LocalBoopPalette.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        textStyle = TextStyle(
            fontFamily = BoopSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = palette.onBackground,
            textDecoration = titleDecoration,
        ),
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = KeepSentenceKeyboard,
        singleLine = true,
        decorationBox = { inner ->
            Box {
                if (value.isBlank()) {
                    Text(
                        placeholder,
                        style = TextStyle(
                            fontFamily = BoopSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = palette.muted.copy(alpha = 0.65f),
                        ),
                    )
                }
                inner()
            }
        },
    )
}

@Composable
fun KeepBorderlessBodyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 4,
) {
    val palette = LocalBoopPalette.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        textStyle = TextStyle(
            fontFamily = BoopSansFamily,
            fontSize = 16.sp,
            color = palette.onBackground,
            lineHeight = 22.sp,
        ),
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = KeepSentenceKeyboard,
        minLines = minLines,
        decorationBox = { inner ->
            Box {
                if (value.isBlank()) {
                    Text(placeholder, color = palette.muted.copy(alpha = 0.55f), style = MaterialTheme.typography.bodyLarge)
                }
                inner()
            }
        },
    )
}

@Composable
fun KeepEditorDockedBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Column(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 12.dp)
                .background(palette.surfaceBorder.copy(alpha = 0.65f)),
        )
        KeepEditorBottomBar(
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeepNoteLabelChips(
    tagsCsv: String,
    modifier: Modifier = Modifier,
    onRemoveTag: ((String) -> Unit)? = null,
) {
    val tags = parseNoteTagsForEditor(tagsCsv)
    if (tags.isEmpty()) return
    FlowRow(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            KeepPaletteChip(
                label = tag,
                selected = true,
                onClick = { onRemoveTag?.invoke(tag) },
            )
        }
    }
}

@Composable
fun KeepNoteTagDock(
    tagsCsv: String,
    onTagsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val tags = parseNoteTagsForEditor(tagsCsv)
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (tags.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tags.forEach { tag ->
                    KeepPaletteChip(
                        label = tag,
                        selected = true,
                        onClick = {
                            onTagsChange(
                                tags.filterNot { it.equals(tag, ignoreCase = true) }.joinToString(", "),
                            )
                        },
                    )
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.surfaceVariant,
            border = BorderStroke(1.dp, palette.surfaceBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicTextField(
                value = tagsCsv,
                onValueChange = onTagsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                textStyle = TextStyle(
                    fontFamily = BoopSansFamily,
                    fontSize = 14.sp,
                    color = palette.onBackground,
                ),
                cursorBrush = SolidColor(palette.accent),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (tagsCsv.isBlank()) {
                            Text(
                                "Add labels (comma separated)",
                                color = palette.muted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        inner()
                    }
                },
            )
        }
    }
}

fun parseNoteTagsForEditor(raw: String): List<String> =
    raw.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase(java.util.Locale.getDefault()) }

fun htmlNoteBodyToPlain(body: String): String {
    if (isChecklistBody(body)) return ""
    val trimmed = body.trim()
    if (trimmed.isBlank()) return ""
    return if (trimmed.contains('<')) {
        androidx.core.text.HtmlCompat.fromHtml(trimmed, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace('\u00a0', ' ')
            .trim()
    } else {
        trimmed
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeepEditorBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}

@Composable
fun KeepNoteFormatToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrike: () -> Unit,
    onBullets: () -> Unit,
    onNumbers: () -> Unit,
    onHeading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeepToolbarIconButton(onClick = onBold, icon = Icons.Outlined.FormatBold, contentDescription = "Bold")
        KeepToolbarIconButton(onClick = onItalic, icon = Icons.Outlined.FormatItalic, contentDescription = "Italic")
        KeepToolbarIconButton(onClick = onUnderline, icon = Icons.Outlined.FormatUnderlined, contentDescription = "Underline")
        KeepToolbarIconButton(onClick = onStrike, icon = Icons.Outlined.FormatStrikethrough, contentDescription = "Strikethrough")
        KeepToolbarIconButton(onClick = onBullets, icon = Icons.Outlined.FormatListBulleted, contentDescription = "Bulleted list")
        KeepToolbarIconButton(onClick = onNumbers, icon = Icons.Outlined.FormatListNumbered, contentDescription = "Numbered list")
        KeepToolbarIconButton(onClick = onHeading, icon = Icons.Outlined.Title, contentDescription = "Heading")
    }
}

@Composable
fun KeepNoteEditDock(
    showFormat: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrike: () -> Unit,
    onBullets: () -> Unit,
    onNumbers: () -> Unit,
    onHeading: () -> Unit,
    modifier: Modifier = Modifier,
    insertActions: @Composable () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.sheetBg,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.surfaceBorder),
            )
            if (showFormat) {
                KeepNoteFormatToolbar(
                    onBold = onBold,
                    onItalic = onItalic,
                    onUnderline = onUnderline,
                    onStrike = onStrike,
                    onBullets = onBullets,
                    onNumbers = onNumbers,
                    onHeading = onHeading,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                insertActions()
            }
        }
    }
}

@Composable
fun KeepEditorMetaLine(text: String, modifier: Modifier = Modifier) {
    val palette = LocalBoopPalette.current
    Text(
        text,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        color = palette.muted,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
fun KeepSectionLabel(text: String, modifier: Modifier = Modifier) {
    val palette = LocalBoopPalette.current
    Text(
        text,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        color = palette.muted,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun KeepEditorActionCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = palette.surfaceVariant,
        border = BorderStroke(1.dp, palette.surfaceBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(label, color = palette.muted, style = MaterialTheme.typography.labelMedium)
            Text(value, color = palette.onBackground, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun KeepEditorSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.onBackground, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.accentOn,
                checkedTrackColor = palette.accent,
                uncheckedThumbColor = palette.muted,
                uncheckedTrackColor = palette.surfaceVariant,
            ),
        )
    }
}

@Composable
fun KeepPaletteChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) palette.accent else palette.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) palette.accent else palette.surfaceBorder),
        modifier = modifier,
    ) {
        Text(
            label,
            color = if (selected) palette.accentOn else palette.muted,
            style = MaterialTheme.typography.labelSmall,
            modifier = modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeepEditorRepeatSection(
    repeatEveryDays: Int,
    customRepeatDays: String,
    onRepeatEveryDaysChange: (Int) -> Unit,
    onCustomRepeatDaysChange: (String) -> Unit,
    frequencyLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val repeatOptions = listOf(
        0 to "None",
        1 to "Daily",
        7 to "Weekly",
        30 to "Monthly",
        365 to "Yearly",
    )
    Column(modifier) {
        KeepSectionLabel("Repeat")
        FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeatOptions.forEach { (days, label) ->
                KeepPaletteChip(
                    label = label,
                    selected = repeatEveryDays == days,
                    onClick = {
                        onRepeatEveryDaysChange(days)
                        if (days in setOf(0, 1, 7, 30, 365)) onCustomRepeatDaysChange("")
                    },
                )
            }
            val customActive = repeatEveryDays !in setOf(0, 1, 7, 30, 365)
            KeepPaletteChip(
                label = "Custom",
                selected = customActive,
                onClick = {
                    if (repeatEveryDays in setOf(0, 1, 7, 30, 365)) onRepeatEveryDaysChange(2)
                },
            )
        }
        if (repeatEveryDays !in setOf(0, 1, 7, 30, 365)) {
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = customRepeatDays,
                onValueChange = {
                    val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                    onCustomRepeatDaysChange(filtered)
                    filtered.toIntOrNull()?.coerceAtLeast(1)?.let(onRepeatEveryDaysChange)
                },
                label = { Text("Every N days") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = KeepOutlinedFieldColors(),
            )
        }
        if (!frequencyLabel.isNullOrBlank()) {
            KeepEditorMetaLine(frequencyLabel)
        }
    }
}

@Composable
fun KeepOutlinedFieldColors() = run {
    val palette = LocalBoopPalette.current
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = palette.accent,
        unfocusedBorderColor = palette.surfaceBorder,
        focusedTextColor = palette.onBackground,
        unfocusedTextColor = palette.onBackground,
        cursorColor = palette.accent,
        focusedLabelColor = palette.muted,
        unfocusedLabelColor = palette.muted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepLinkInputSheet(
    open: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    if (!open) return
    val palette = LocalBoopPalette.current
    val clipboard = LocalClipboardManager.current
    var url by rememberSaveable { mutableStateOf("https://") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.sheetBg,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                "Add link",
                color = palette.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = BoopSerifFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                "Paste a web address or type a URL",
                color = palette.muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://example.com", color = palette.muted.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Link, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val trimmed = url.trim()
                    if (trimmed.isNotBlank() && trimmed != "https://") {
                        onAdd(trimmed)
                        onDismiss()
                    }
                }),
                colors = KeepOutlinedFieldColors(),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = {
                        val clip = clipboard.getText()?.text?.trim().orEmpty()
                        if (clip.isNotBlank()) url = clip
                    },
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = null, tint = palette.muted, modifier = Modifier.size(16.dp))
                    Text("Paste", color = palette.muted, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = palette.muted)
                }
                Surface(
                    onClick = {
                        val trimmed = url.trim()
                        if (trimmed.isNotBlank() && trimmed != "https://") {
                            onAdd(trimmed)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = palette.accent,
                ) {
                    Text(
                        "Add link",
                        color = palette.accentOn,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun KeepLinkPreviewCard(
    link: String,
    title: String?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val host = remember(link) { runCatching { android.net.Uri.parse(link).host.orEmpty() }.getOrDefault("") }
    val displayTitle = title?.takeIf { it.isNotBlank() } ?: host.ifBlank { "Link" }
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(14.dp),
        color = palette.surfaceVariant,
        border = BorderStroke(1.dp, palette.surfaceBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.chipBg,
                border = BorderStroke(1.dp, palette.surfaceBorder),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Link, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    displayTitle,
                    color = palette.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (host.isNotBlank()) {
                    Text(
                        host,
                        color = palette.muted,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(Icons.Outlined.OpenInNew, contentDescription = "Open link", tint = palette.muted, modifier = Modifier.size(18.dp))
        }
    }
}
