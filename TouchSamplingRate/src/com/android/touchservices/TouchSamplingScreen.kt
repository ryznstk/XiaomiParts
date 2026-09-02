/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.touchservices

import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieAnimationView
import com.android.touchservices.R
import com.android.touchservices.ui.AppIcon
import com.android.touchservices.ui.components.SettingsSectionCard
import com.android.touchservices.ui.components.SettingsToggleRow
import com.android.settingslib.widget.LottieColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class TouchSamplingApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val enabled: Boolean
)

@Composable
fun TouchSamplingScreen() {
    val context = LocalContext.current
    val apps = remember { mutableStateListOf<TouchSamplingApp>() }
    var supported by remember { mutableStateOf(TouchSamplingUtils.isSupported()) }
    var enabled by remember { mutableStateOf(TouchSamplingUtils.isEnabled(context)) }
    var autoEnabled by remember { mutableStateOf(TouchSamplingUtils.isAutoEnabled(context)) }
    var searchQuery by remember { mutableStateOf("") }

    suspend fun loadApps() {
        if (!supported) {
            apps.clear()
            return
        }
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        val selectedApps = TouchSamplingUtils.getAutoApps(context)
        val loaded = withContext(Dispatchers.IO) {
            launcherApps
                ?.getActivityList(null, Process.myUserHandle())
                ?.distinctBy { it.componentName.packageName }
                ?.map { info ->
                    TouchSamplingApp(
                        packageName = info.componentName.packageName,
                        label = info.label.toString(),
                        icon = info.getIcon(0),
                        enabled = selectedApps.contains(info.componentName.packageName)
                    )
                }
                ?.sortedBy { it.label.lowercase() }
                ?: emptyList()
        }
        apps.clear()
        apps.addAll(loaded)
    }

    fun syncService() {
        TouchSamplingUtils.syncService(context)
        supported = TouchSamplingUtils.isSupported()
    }

    LaunchedEffect(Unit) {
        loadApps()
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (supported) {
            item {
                TouchSamplingAnimationCard()
            }
        }

        item {
            SettingsSectionCard(
                title = stringResource(R.string.touch_sampling_title),
                summary = stringResource(R.string.touch_sampling_summary)
            ) {
                if (!supported) {
                    Text(
                        text = stringResource(R.string.touch_sampling_not_supported),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    SettingsToggleRow(
                        title = stringResource(R.string.touch_sampling_enable_title),
                        summary = stringResource(R.string.touch_sampling_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            TouchSamplingUtils.setEnabled(context, it)
                            syncService()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsToggleRow(
                        title = stringResource(R.string.touch_sampling_auto_title),
                        summary = stringResource(R.string.touch_sampling_auto_summary),
                        checked = autoEnabled,
                        onCheckedChange = {
                            autoEnabled = it
                            TouchSamplingUtils.setAutoEnabled(context, it)
                            syncService()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.touch_sampling_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (supported && autoEnabled) {
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.touch_sampling_apps_title),
                    summary = stringResource(R.string.touch_sampling_apps_summary)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = stringResource(R.string.search_apps))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            items(
                items = apps.filter { app ->
                    if (searchQuery.isBlank()) return@filter true
                    val query = searchQuery.trim().lowercase()
                    app.label.lowercase().contains(query) || app.packageName.lowercase().contains(query)
                },
                key = { it.packageName }
            ) { app ->
                TouchSamplingAppItem(
                    app = app,
                    onCheckedChange = { checked ->
                        TouchSamplingUtils.setAppEnabled(context, app.packageName, checked)
                        val index = apps.indexOfFirst { it.packageName == app.packageName }
                        if (index != -1) {
                            apps[index] = app.copy(enabled = checked)
                        }
                        syncService()
                    }
                )
            }
        }
    }
}

@Composable
private fun TouchSamplingAnimationCard() {
    SettingsSectionCard(
        title = stringResource(R.string.touch_sampling_title),
        summary = null,
        showHeader = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    LottieAnimationView(context).apply {
                        setAnimation(R.raw.htsr)
                        setBackgroundColor(android.graphics.Color.BLACK)
                        LottieColorUtils.applyDynamicColors(context, this)
                        repeatCount = LottieDrawable.INFINITE
                        repeatMode = LottieDrawable.RESTART
                        playAnimation()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

@Composable
private fun TouchSamplingAppItem(
    app: TouchSamplingApp,
    onCheckedChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                drawable = app.icon,
                contentDescription = app.label,
                modifier = Modifier.size(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = app.enabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
