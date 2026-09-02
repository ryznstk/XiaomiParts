/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.touchservices

import android.app.ActivityTaskManager
import android.app.Service
import android.app.TaskStackListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder
import androidx.preference.PreferenceManager

class TouchSamplingService : Service() {

    private var currentApp: String? = null
        set(value) {
            if (field == value) return
            field = value
            refresh()
        }

    private var taskListenerRegistered = false
    private var receiverRegistered = false
    private lateinit var prefs: SharedPreferences
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val taskListener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            runCatching {
                currentApp = ActivityTaskManager.getService().focusedRootTaskInfo?.topActivity?.packageName
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_USER_PRESENT,
                Intent.ACTION_SCREEN_ON -> {
                    taskListener.onTaskStackChanged()
                    refresh()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (
                key == TouchSamplingUtils.PREF_ENABLED ||
                key == TouchSamplingUtils.PREF_AUTO_ENABLE ||
                key == TouchSamplingUtils.PREF_AUTO_APPS
            ) {
                TouchSamplingUtils.syncService(this)
                refresh()
            }
        }
        prefListener?.let { prefs.registerOnSharedPreferenceChangeListener(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!TouchSamplingUtils.isSupported()) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!taskListenerRegistered) {
            taskListenerRegistered = runCatching {
                ActivityTaskManager.getService().registerTaskStackListener(taskListener)
            }.isSuccess
        }

        if (!receiverRegistered) {
            receiverRegistered = runCatching {
                registerReceiver(
                    receiver,
                    IntentFilter().apply {
                        addAction(Intent.ACTION_USER_PRESENT)
                        addAction(Intent.ACTION_SCREEN_ON)
                    }
                )
            }.isSuccess
        }

        taskListener.onTaskStackChanged()
        refresh()
        return START_STICKY
    }

    override fun onDestroy() {
        prefListener?.let { prefs.unregisterOnSharedPreferenceChangeListener(it) }
        prefListener = null
        if (receiverRegistered) {
            runCatching { unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        if (taskListenerRegistered) {
            runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
            taskListenerRegistered = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun refresh() {
        val shouldRun = TouchSamplingUtils.isEnabled(this) || TouchSamplingUtils.isAutoEnabled(this)
        if (!shouldRun) {
            TouchSamplingUtils.writeState(false)
            stopSelf()
            return
        }
        TouchSamplingUtils.applyNow(this, currentApp)
    }
}
