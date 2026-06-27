package com.prodash.reminders

import android.content.Context
import android.graphics.Typeface
import android.widget.EditText
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.delay

/** Claude UI sans — Inter static cuts per weight. */
val BoopSansFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Claude editorial serif — Source Serif 4. */
val BoopSerifFamily = FontFamily(
    Font(R.font.source_serif_4, FontWeight.Normal),
    Font(R.font.source_serif_4_medium, FontWeight.Medium),
    Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
)

fun boopTypography(): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.6).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.4).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = BoopSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BoopSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
    ),
)

fun Context.boopSansTypeface(weight: FontWeight = FontWeight.Normal): Typeface? {
    val resId = when (weight) {
        FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black -> R.font.inter_bold
        FontWeight.SemiBold -> R.font.inter_semibold
        FontWeight.Medium -> R.font.inter_medium
        else -> R.font.inter_regular
    }
    return ResourcesCompat.getFont(this, resId)
}

fun Context.boopSerifTypeface(weight: FontWeight = FontWeight.Normal): Typeface? {
    val resId = when (weight) {
        FontWeight.SemiBold, FontWeight.Bold -> R.font.source_serif_4_semibold
        FontWeight.Medium -> R.font.source_serif_4_medium
        else -> R.font.source_serif_4
    }
    return ResourcesCompat.getFont(this, resId)
}

fun TextView.applyBoopSans(weight: FontWeight = FontWeight.Normal) {
    context.boopSansTypeface(weight)?.let { typeface = it }
}

fun EditText.applyBoopSans(weight: FontWeight = FontWeight.Normal) {
    context.boopSansTypeface(weight)?.let { typeface = it }
}

/** Ensures every [Text] without an explicit style uses Inter body text. */
@Composable
fun BoopTextTheme(content: @Composable () -> Unit) {
    val body = MaterialTheme.typography.bodyMedium
    androidx.compose.runtime.CompositionLocalProvider(
        LocalTextStyle provides body.copy(fontFamily = BoopSansFamily),
    ) {
        content()
    }
}

private val BoopEnterTransition = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
    slideInVertically(
        initialOffsetY = { it / 5 },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    )

@Composable
fun BoopAnimatedEnter(
    key: Any? = Unit,
    animated: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(!animated) }
    LaunchedEffect(key, animated) {
        if (!animated) {
            visible = true
        } else {
            visible = false
            delay(16)
            visible = true
        }
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = BoopEnterTransition,
    ) {
        content()
    }
}

@Composable
fun BoopPageTitle(text: String, modifier: Modifier = Modifier, animated: Boolean = true) {
    BoopAnimatedEnter(key = text, animated = animated, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun BoopFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun BoopSheetHeaderTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun BoopText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}
