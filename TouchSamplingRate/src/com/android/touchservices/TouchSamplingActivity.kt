/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.touchservices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import com.android.touchservices.R
import com.android.touchservices.ui.TouchserviceScaffold
import com.android.touchservices.ui.theme.TouchserviceTheme

class TouchSamplingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TouchserviceTheme {
                TouchserviceScaffold(
                    title = getString(R.string.touch_sampling_title),
                    subtitle = getString(R.string.touch_sampling_summary),
                    icon = Icons.Rounded.TouchApp,
                    showBack = true,
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                ) {
                    TouchSamplingScreen()
                }
            }
        }
    }
}
