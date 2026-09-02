/*
 * SPDX-FileCopyrightText: 2025 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.touchservices.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.touchservices.ui.theme.UiStyleController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchserviceScaffold(
    title: String,
    subtitle: String?,
    tagline: String? = null,
    icon: ImageVector,
    showBack: Boolean,
    showTopBar: Boolean = true,
    onBack: () -> Unit,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    UiStyleController.ensureInitialized(androidx.compose.ui.platform.LocalContext.current)
    val blurEnabled by UiStyleController.blurEnabled.collectAsState()
    val headerTextColor = MaterialTheme.colorScheme.onSurface
    val headerTextShadow = rememberHeaderTextShadow()
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.withHeaderShadow(headerTextShadow),
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            if (!tagline.isNullOrBlank()) {
                                Text(
                                    text = tagline,
                                    style = MaterialTheme.typography.labelMedium.withHeaderShadow(headerTextShadow),
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    color = headerTextColor.copy(alpha = 0.92f),
                                    modifier = Modifier.padding(start = 30.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AnimatedHeaderCard(
                    title = title,
                    subtitle = subtitle,
                    tagline = tagline,
                    icon = icon,
                    blurEnabled = blurEnabled,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    color = if (blurEnabled) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    content(padding)
                }
            }
            if (bottomBar != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeaderCard(
    title: String,
    subtitle: String?,
    tagline: String?,
    icon: ImageVector,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val headerTextColor = MaterialTheme.colorScheme.onSurface
    val headerSubtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    val headerTextShadow = rememberHeaderTextShadow()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut()
    ) {
        val transition = rememberInfiniteTransition(label = "header_gradient")
        val shift by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing)
            ),
            label = "header_shift"
        )
        Card(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (blurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .blur(0.2.dp)
                    } else {
                        Modifier
                    }
                ),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = if (blurEnabled) {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.64f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(900f * shift, 600f * (1.1f - shift))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.withHeaderShadow(headerTextShadow),
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium.withHeaderShadow(headerTextShadow),
                                    color = headerSubtitleColor
                                )
                            }
                            if (!tagline.isNullOrBlank()) {
                                Text(
                                    text = tagline,
                                    style = MaterialTheme.typography.labelMedium.withHeaderShadow(headerTextShadow),
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    color = headerSubtitleColor,
                                    modifier = Modifier.padding(start = 30.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun rememberHeaderTextShadow(): Shadow {
    return Shadow(
        color = Color.Black.copy(alpha = 0.42f),
        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
        blurRadius = 8f
    )
}

private fun TextStyle.withHeaderShadow(shadow: Shadow): TextStyle = copy(shadow = shadow)
