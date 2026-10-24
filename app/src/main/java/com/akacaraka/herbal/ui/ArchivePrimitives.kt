@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.akacaraka.herbal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun Modifier.pageWidth(maxWidth: Dp): Modifier = widthIn(max = maxWidth).fillMaxWidth()

internal fun Modifier.buttonMaxWidth(spec: AdaptiveSpec): Modifier = if (spec.isCompact) fillMaxWidth() else widthIn(max = 280.dp)

internal val BottomDockHeight: Dp = 84.dp
internal val BottomNavClearance: Dp = BottomDockHeight + 260.dp

@Composable
internal fun HerbTextAction(text: String, onClick: () -> Unit, color: Color, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier.sizeIn(minWidth = HerbSpace.TouchTarget, minHeight = HerbSpace.TouchTarget)) {
        Text(text, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun AppPageColumn(
    spec: AdaptiveSpec,
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(HerbColors.IvoryGround).navigationBarsPadding().imePadding(),
        state = scrollState,
        contentPadding = PaddingValues(
            start = spec.pagePadding,
            top = spec.sectionGap,
            end = spec.pagePadding,
            bottom = spec.sectionGap
        ),
        verticalArrangement = Arrangement.spacedBy(spec.sectionGap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
        item { Spacer(Modifier.height(BottomNavClearance)) }
    }
}

@Composable
internal fun ArchivePanel(
    spec: AdaptiveSpec,
    modifier: Modifier = Modifier,
    background: Color = HerbColors.RicePaper,
    strong: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier.fillMaxWidth(),
        color = background,
        shape = if (strong) HerbRadius.Lg else HerbRadius.Md,
        tonalElevation = if (strong) 1.dp else 0.dp,
        shadowElevation = if (strong) 2.dp else 1.dp
    ) {
        Column(Modifier.padding(spec.panelPadding), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3), content = content)
    }
}

@Composable
internal fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        Icon(HerbIcons.Leaf(), contentDescription = null, tint = HerbColors.TerracottaAccent, modifier = Modifier.height(HerbSpace.Space4))
        Text(text, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun SectionTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = HerbColors.ArchiveInk, modifier = Modifier.semantics { heading() })
        Text(subtitle, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun HeroText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.displaySmall)
}

internal enum class HerbButtonTone { Primary, Secondary, Moss, Terracotta, Destructive }

@Composable
internal fun HerbButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    tone: HerbButtonTone = HerbButtonTone.Primary
) {
    val container = when (tone) {
        HerbButtonTone.Primary -> HerbColors.ArchiveInk
        HerbButtonTone.Secondary -> HerbColors.MossSoft
        HerbButtonTone.Moss -> HerbColors.MossPrimary
        HerbButtonTone.Terracotta -> HerbColors.TerracottaAccent
        HerbButtonTone.Destructive -> HerbColors.TerracottaSoft
    }
    val content = when (tone) {
        HerbButtonTone.Secondary -> HerbColors.ArchiveInk
        HerbButtonTone.Destructive -> HerbColors.BrickError
        else -> HerbColors.RicePaper
    }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(HerbSpace.TouchTarget).sizeIn(minWidth = HerbSpace.TouchTarget),
        shape = HerbRadius.Sm,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content, disabledContainerColor = HerbColors.RattanLine, disabledContentColor = HerbColors.MutedInk)
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.height(HerbSpace.Space5), color = content, strokeWidth = 2.dp)
        Text(if (loading) "$text..." else text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun HerbTextField(
    value: String,
    label: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    error: String = "",
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        isError = error.isNotBlank(),
        supportingText = { if (error.isNotBlank()) Text(error) },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null, tint = HerbColors.MutedInk) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HerbColors.FocusGold,
            focusedLabelColor = HerbColors.ArchiveInk,
            cursorColor = HerbColors.TerracottaAccent,
            unfocusedBorderColor = HerbColors.RattanLine,
            focusedContainerColor = HerbColors.RicePaper,
            unfocusedContainerColor = HerbColors.RicePaper,
            errorBorderColor = HerbColors.BrickError,
            errorLabelColor = HerbColors.BrickError
        ),
        shape = HerbRadius.Sm
    )
}

@Composable
internal fun EmptyState(label: String, body: String, spec: AdaptiveSpec, action: (@Composable () -> Unit)? = null) {
    ArchivePanel(spec) {
        SectionEyebrow(label)
        Text(body, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyLarge)
        action?.invoke()
    }
}

@Composable
internal fun LoadingState(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(HerbSpace.Space3), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.height(HerbSpace.Space5), color = HerbColors.MossPrimary, strokeWidth = 2.dp)
        Text(text, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun FlowActions(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) { content() }
}

@Composable
internal fun ThinMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = HerbColors.IvoryGround, shape = HerbRadius.Sm, shadowElevation = 1.dp) {
    Column(Modifier.padding(horizontal = HerbSpace.Space2, vertical = HerbSpace.Space3), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    }
}

@Composable
internal fun SpecimenFallback(name: String, modifier: Modifier, corner: RoundedCornerShape = HerbRadius.Sm) {
    Box(modifier.background(HerbColors.MossSoft, corner), contentAlignment = Alignment.Center) {
        Text(name.ifBlank { "Herbal" }, modifier = Modifier.padding(HerbSpace.Space4), color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

internal fun Modifier.archiveClickable(description: String, onClick: () -> Unit): Modifier = semantics {
    role = Role.Button
    contentDescription = description
}.clip(HerbRadius.Sm).clickable(onClick = onClick)
