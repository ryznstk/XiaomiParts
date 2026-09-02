/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.touchservices

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.android.touchservices.R

class TouchSamplingTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        if (!TouchSamplingUtils.isSupported()) {
            updateTile()
            return
        }
        TouchSamplingUtils.setEnabled(this, !TouchSamplingUtils.isEnabled(this))
        TouchSamplingUtils.syncService(this)
        updateTile()
        super.onClick()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val supported = TouchSamplingUtils.isSupported()
        val enabled = supported && TouchSamplingUtils.isEnabled(this)

        tile.label = getString(R.string.touch_sampling_title)
        tile.subtitle = when {
            !supported -> getString(R.string.touch_sampling_not_supported)
            enabled -> getString(R.string.tile_on)
            else -> getString(R.string.tile_off)
        }
        tile.state = when {
            !supported -> Tile.STATE_UNAVAILABLE
            enabled -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_touch_sampling_tile)
        tile.updateTile()
    }
}
