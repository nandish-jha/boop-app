package com.prodash.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

const val BOOP_CHECKLIST_MARKER = "<!--boop-checklist-->"

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val done: Boolean = false,
)

fun isChecklistBody(body: String): Boolean = body.trimStart().startsWith(BOOP_CHECKLIST_MARKER)

fun parseChecklistBody(body: String): List<ChecklistItem> {
    if (!isChecklistBody(body)) return emptyList()
    val raw = body.trimStart().removePrefix(BOOP_CHECKLIST_MARKER).trim()
    if (raw.isBlank()) return listOf(ChecklistItem(text = ""))
    return runCatching {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    ChecklistItem(
                        id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                        text = o.optString("text"),
                        done = o.optBoolean("done", false),
                    ),
                )
            }
        }.ifEmpty { listOf(ChecklistItem(text = "")) }
    }.getOrElse { listOf(ChecklistItem(text = "")) }
}

fun serializeChecklistBody(items: List<ChecklistItem>): String {
    val arr = JSONArray()
    items.filter { it.text.isNotBlank() || items.size == 1 }.forEach { item ->
        arr.put(
            JSONObject()
                .put("id", item.id)
                .put("text", item.text)
                .put("done", item.done),
        )
    }
    if (arr.length() == 0) {
        arr.put(JSONObject().put("id", UUID.randomUUID().toString()).put("text", "").put("done", false))
    }
    return "$BOOP_CHECKLIST_MARKER\n${arr.toString()}"
}

fun plainTextFromBody(body: String): String =
    if (isChecklistBody(body)) "" else body

fun parseSubtasksJson(json: String): List<ChecklistItem> {
    if (json.isBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    ChecklistItem(
                        id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                        text = o.optString("text"),
                        done = o.optBoolean("done", false),
                    ),
                )
            }
        }
    }.getOrElse { emptyList() }
}

fun serializeSubtasksJson(items: List<ChecklistItem>): String {
    val arr = JSONArray()
    items.filter { it.text.isNotBlank() }.forEach { item ->
        arr.put(
            JSONObject()
                .put("id", item.id)
                .put("text", item.text)
                .put("done", item.done),
        )
    }
    return arr.toString()
}

@Composable
fun KeepChecklistEditor(
    items: List<ChecklistItem>,
    onChange: (List<ChecklistItem>) -> Unit,
    modifier: Modifier = Modifier,
    editing: Boolean = true,
) {
    val palette = LocalBoopPalette.current
    androidx.compose.foundation.layout.Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            key(item.id) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        onClick = {
                            if (editing) {
                                onChange(items.map { if (it.id == item.id) it.copy(done = !it.done) else it })
                            }
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (item.done) palette.accent else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(2.dp, palette.accent),
                        modifier = Modifier.size(22.dp),
                    ) {
                        if (item.done) {
                            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    BasicTextField(
                        value = item.text,
                        onValueChange = { next ->
                            onChange(items.map { if (it.id == item.id) it.copy(text = next) else it })
                        },
                        enabled = editing,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontFamily = BoopSansFamily,
                            fontSize = 16.sp,
                            color = palette.onBackground,
                            textDecoration = if (item.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        ),
                        cursorBrush = SolidColor(palette.accent),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (item.text.isBlank()) {
                                Text("List item", color = palette.muted.copy(alpha = 0.55f), style = MaterialTheme.typography.bodyMedium)
                            }
                            inner()
                        },
                    )
                    if (editing) {
                        KeepToolbarIconButton(
                            onClick = { onChange(items.filterNot { it.id == item.id }) },
                            icon = Icons.Outlined.Close,
                            contentDescription = "Remove item",
                            tint = palette.muted,
                        )
                    }
                }
            }
        }
        if (editing) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KeepToolbarIconButton(
                    onClick = { onChange(items + ChecklistItem(text = "")) },
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add list item",
                    tint = palette.muted,
                )
                Text("Add item", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
