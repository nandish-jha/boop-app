package com.prodash.reminders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

enum class NoteInlineFormat {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKE,
}

private val bulletLead = Regex("""^\s*•\s+""")
private val numberedLead = Regex("""^\s*\d+\.\s+""")
private val headingLead = Regex("""^\s*#\s+""")

/** Wrap/unwrap the current selection with markdown-lite markers. */
fun TextFieldValue.toggleInlineFormat(format: NoteInlineFormat): TextFieldValue {
    val text = text
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)
    val (open, close) = when (format) {
        NoteInlineFormat.BOLD -> "**" to "**"
        NoteInlineFormat.ITALIC -> "*" to "*"
        NoteInlineFormat.UNDERLINE -> "__" to "__"
        NoteInlineFormat.STRIKE -> "~~" to "~~"
    }
    if (end > start) {
        val selected = text.substring(start, end)
        val unwrapped = unwrapMarkers(selected, open, close)
        val replacement = if (unwrapped != null) {
            unwrapped
        } else {
            "$open$selected$close"
        }
        val newText = text.replaceRange(start, end, replacement)
        val cursor = start + replacement.length
        return TextFieldValue(newText, TextRange(cursor))
    }
    val insert = "$open$close"
    val newText = text.replaceRange(start, start, insert)
    val cursor = start + open.length
    return TextFieldValue(newText, TextRange(cursor))
}

private fun unwrapMarkers(selected: String, open: String, close: String): String? {
    if (selected.startsWith(open) && selected.endsWith(close) && selected.length >= open.length + close.length) {
        return selected.removePrefix(open).removeSuffix(close)
    }
    return null
}

fun TextFieldValue.toggleBulletLines(): TextFieldValue = toggleLinePrefix(
    applyPrefix = { "• $it" },
    lead = bulletLead,
)

fun TextFieldValue.toggleNumberedLines(): TextFieldValue {
    val text = text
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)
    if (end > start) {
        val selected = text.substring(start, end)
        val lines = selected.split('\n')
        val nonBlank = lines.filter { it.isNotBlank() }
        val stripAll = nonBlank.isNotEmpty() && nonBlank.all { numberedLead.containsMatchIn(it) }
        val replaced = if (stripAll) {
            lines.joinToString("\n") { line ->
                if (line.isBlank()) line else line.replaceFirst(numberedLead, "")
            }
        } else {
            var idx = 1
            lines.joinToString("\n") { line ->
                if (line.isBlank()) line else "${idx++}. ${line.replaceFirst(numberedLead, "").trimStart()}"
            }
        }
        val newText = text.replaceRange(start, end, replaced)
        return TextFieldValue(newText, TextRange(start + replaced.length))
    }
    return insertAtLineStart("1. ")
}

fun TextFieldValue.toggleHeadingLine(): TextFieldValue = toggleLinePrefix(
    applyPrefix = { "# $it" },
    lead = headingLead,
)

private fun TextFieldValue.toggleLinePrefix(
    applyPrefix: (String) -> String,
    lead: Regex,
): TextFieldValue {
    val text = text
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)
    if (end > start) {
        val selected = text.substring(start, end)
        val lines = selected.split('\n')
        val nonBlank = lines.filter { it.isNotBlank() }
        val stripAll = nonBlank.isNotEmpty() && nonBlank.all { lead.containsMatchIn(it) }
        val replaced = lines.joinToString("\n") { line ->
            when {
                line.isBlank() -> line
                stripAll -> line.replaceFirst(lead, "")
                else -> applyPrefix(line.replaceFirst(lead, "").trimStart())
            }
        }
        val newText = text.replaceRange(start, end, replaced)
        return TextFieldValue(newText, TextRange(start + replaced.length))
    }
    val prefix = when {
        lead.pattern.contains("•") -> "• "
        lead.pattern.contains("#") -> "# "
        else -> "1. "
    }
    return insertAtLineStart(prefix)
}

private fun TextFieldValue.insertAtLineStart(prefix: String): TextFieldValue {
    val text = text
    val pos = selection.min.coerceIn(0, text.length)
    val lineStart = text.lastIndexOf('\n', (pos - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', pos).let { if (it < 0) text.length else it }
    val line = text.substring(lineStart, lineEnd)
    val newLine = when {
        bulletLead.containsMatchIn(line) && prefix.startsWith("•") ->
            line.replaceFirst(bulletLead, "")
        numberedLead.containsMatchIn(line) && prefix.first().isDigit() ->
            line.replaceFirst(numberedLead, "")
        headingLead.containsMatchIn(line) && prefix.startsWith("#") ->
            line.replaceFirst(headingLead, "")
        else -> {
            val cleaned = line
                .replaceFirst(bulletLead, "")
                .replaceFirst(numberedLead, "")
                .replaceFirst(headingLead, "")
            "$prefix$cleaned"
        }
    }
    val newText = text.replaceRange(lineStart, lineEnd, newLine)
    val cursor = (lineStart + newLine.length).coerceAtMost(newText.length)
    return TextFieldValue(newText, TextRange(cursor))
}

/** Strip markup markers for plain snippets / search. */
fun stripNoteMarkup(raw: String): String {
    if (raw.isBlank()) return ""
    if (isChecklistBody(raw)) {
        val items = parseChecklistBody(raw)
        if (items.isEmpty()) return "Empty checklist"
        val done = items.count { it.done }
        val preview = items.filter { it.text.isNotBlank() }.take(3)
            .joinToString(" · ") { item ->
                val mark = if (item.done) "✓" else "○"
                "$mark ${item.text.trim()}"
            }
        return if (preview.isBlank()) "$done/${items.size} done" else "$preview · $done/${items.size}"
    }
    var text = raw
    if (text.contains('<')) {
        text = androidx.core.text.HtmlCompat.fromHtml(text, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace('\u00a0', ' ')
    }
    return text
        .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
        .replace(Regex("""__(.+?)__"""), "$1")
        .replace(Regex("""~~(.+?)~~"""), "$1")
        .replace(Regex("""(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)"""), "$1")
        .replace(Regex("""(?m)^\s*#\s+"""), "")
        .replace('\n', ' ')
        .replace(Regex("""\s+"""), " ")
        .trim()
}

fun noteBodyAnnotated(raw: String, maxChars: Int = Int.MAX_VALUE): AnnotatedString {
    if (isChecklistBody(raw)) {
        val plain = stripNoteMarkup(raw)
        val clipped = if (plain.length <= maxChars) plain else plain.take(maxChars - 1).trimEnd() + "…"
        return AnnotatedString(clipped)
    }
    val source = if (raw.contains('<') && !raw.contains("**") && !raw.contains("~~")) {
        androidx.core.text.HtmlCompat.fromHtml(raw, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace('\u00a0', ' ')
    } else {
        raw
    }
    val built = buildAnnotatedString {
        appendParsedMarkup(source)
    }
    if (built.length <= maxChars) return built
    return AnnotatedString(built.text.take((maxChars - 1).coerceAtLeast(0)).trimEnd() + "…")
}

private fun AnnotatedString.Builder.appendParsedMarkup(input: String) {
    var i = 0
    while (i < input.length) {
        when {
            input.startsWith("**", i) -> {
                val end = input.indexOf("**", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendParsedMarkup(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i])
                    i++
                }
            }
            input.startsWith("~~", i) -> {
                val end = input.indexOf("~~", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        appendParsedMarkup(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i])
                    i++
                }
            }
            input.startsWith("__", i) -> {
                val end = input.indexOf("__", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        appendParsedMarkup(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i])
                    i++
                }
            }
            input[i] == '*' && !input.startsWith("**", i) -> {
                val end = input.indexOf('*', i + 1)
                if (end > i && (end + 1 >= input.length || input[end + 1] != '*')) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        appendParsedMarkup(input.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(input[i])
                    i++
                }
            }
            input.startsWith("# ", i) && (i == 0 || input[i - 1] == '\n') -> {
                val lineEnd = input.indexOf('\n', i).let { if (it < 0) input.length else it }
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)) {
                    append(input.substring(i + 2, lineEnd))
                }
                i = lineEnd
            }
            else -> {
                append(input[i])
                i++
            }
        }
    }
}

/**
 * Hides markdown markers in the note editor while showing real bold/italic/etc.
 * Markers remain in the underlying stored string.
 */
object NoteMarkupVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val result = buildEditorMarkupTransform(text.text)
        return androidx.compose.ui.text.input.TransformedText(
            text = result.annotated,
            offsetMapping = result.offsetMapping,
        )
    }
}

private data class MarkupTransform(
    val annotated: AnnotatedString,
    val offsetMapping: androidx.compose.ui.text.input.OffsetMapping,
)

private fun buildEditorMarkupTransform(source: String): MarkupTransform {
    val originalToTransformed = IntArray(source.length + 1)
    val transformedToOriginal = mutableListOf<Int>()
    val builder = AnnotatedString.Builder()

    fun emitChar(originalIndex: Int, char: Char, style: SpanStyle? = null) {
        originalToTransformed[originalIndex] = builder.length
        if (style != null) {
            val start = builder.length
            builder.append(char)
            builder.addStyle(style, start, builder.length)
        } else {
            builder.append(char)
        }
        transformedToOriginal.add(originalIndex)
    }

    fun skipMarker(originalIndex: Int) {
        originalToTransformed[originalIndex] = builder.length
    }

    fun process(start: Int, end: Int, inherited: SpanStyle?) {
        var i = start
        while (i < end) {
            val remaining = source.substring(i, end)
            when {
                remaining.startsWith("**") -> {
                    val closeRel = remaining.indexOf("**", 2)
                    if (closeRel > 1) {
                        val contentStart = i + 2
                        val contentEnd = i + closeRel
                        skipMarker(i)
                        skipMarker(i + 1)
                        val bold = SpanStyle(fontWeight = FontWeight.Bold).merge(inherited)
                        process(contentStart, contentEnd, bold)
                        skipMarker(contentEnd)
                        skipMarker(contentEnd + 1)
                        i = contentEnd + 2
                    } else {
                        emitChar(i, source[i], inherited)
                        i++
                    }
                }
                remaining.startsWith("~~") -> {
                    val closeRel = remaining.indexOf("~~", 2)
                    if (closeRel > 1) {
                        val contentStart = i + 2
                        val contentEnd = i + closeRel
                        skipMarker(i); skipMarker(i + 1)
                        val style = SpanStyle(textDecoration = TextDecoration.LineThrough).merge(inherited)
                        process(contentStart, contentEnd, style)
                        skipMarker(contentEnd); skipMarker(contentEnd + 1)
                        i = contentEnd + 2
                    } else {
                        emitChar(i, source[i], inherited)
                        i++
                    }
                }
                remaining.startsWith("__") -> {
                    val closeRel = remaining.indexOf("__", 2)
                    if (closeRel > 1) {
                        val contentStart = i + 2
                        val contentEnd = i + closeRel
                        skipMarker(i); skipMarker(i + 1)
                        val style = SpanStyle(textDecoration = TextDecoration.Underline).merge(inherited)
                        process(contentStart, contentEnd, style)
                        skipMarker(contentEnd); skipMarker(contentEnd + 1)
                        i = contentEnd + 2
                    } else {
                        emitChar(i, source[i], inherited)
                        i++
                    }
                }
                remaining.startsWith("*") && !remaining.startsWith("**") -> {
                    val closeRel = remaining.indexOf('*', 1)
                    if (closeRel > 0 && (closeRel + 1 >= remaining.length || remaining[closeRel + 1] != '*')) {
                        val contentStart = i + 1
                        val contentEnd = i + closeRel
                        skipMarker(i)
                        val style = SpanStyle(fontStyle = FontStyle.Italic).merge(inherited)
                        process(contentStart, contentEnd, style)
                        skipMarker(contentEnd)
                        i = contentEnd + 1
                    } else {
                        emitChar(i, source[i], inherited)
                        i++
                    }
                }
                remaining.startsWith("# ") && (i == 0 || source.getOrNull(i - 1) == '\n') -> {
                    val lineEndRel = remaining.indexOf('\n').let { if (it < 0) remaining.length else it }
                    skipMarker(i); skipMarker(i + 1)
                    val style = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp).merge(inherited)
                    process(i + 2, i + lineEndRel, style)
                    i = i + lineEndRel
                }
                else -> {
                    emitChar(i, source[i], inherited)
                    i++
                }
            }
        }
    }

    process(0, source.length, null)
    originalToTransformed[source.length] = builder.length
    transformedToOriginal.add(source.length)

    val transformedLen = builder.length
    val mapping = object : androidx.compose.ui.text.input.OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val o = offset.coerceIn(0, source.length)
            return originalToTransformed[o].coerceIn(0, transformedLen)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val t = offset.coerceIn(0, transformedToOriginal.lastIndex)
            return transformedToOriginal[t].coerceIn(0, source.length)
        }
    }
    return MarkupTransform(builder.toAnnotatedString(), mapping)
}

private fun SpanStyle.merge(other: SpanStyle?): SpanStyle {
    if (other == null) return this
    val thisDeco = textDecoration
    val otherDeco = other.textDecoration
    val mergedDeco = when {
        thisDeco != null && otherDeco != null -> thisDeco + otherDeco
        thisDeco != null -> thisDeco
        else -> otherDeco
    }
    return SpanStyle(
        color = if (color != androidx.compose.ui.graphics.Color.Unspecified) color else other.color,
        fontSize = if (fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) fontSize else other.fontSize,
        fontWeight = fontWeight ?: other.fontWeight,
        fontStyle = fontStyle ?: other.fontStyle,
        textDecoration = mergedDeco,
    )
}
