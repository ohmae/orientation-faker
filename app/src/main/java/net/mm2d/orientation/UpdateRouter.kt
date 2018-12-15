/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */

object UpdateRouter {
    private const val ACTION_UPDATE = "ACTION_UPDATE"
    @SuppressLint("StaticFieldLeak")
    private lateinit var manager: LocalBroadcastManager

    fun initialize(context: Context) {
        manager = LocalBroadcastManager.getInstance(context)
    }

    fun register(receiver: BroadcastReceiver) {
        manager.registerReceiver(receiver, IntentFilter(ACTION_UPDATE))
    }

    fun unregister(receiver: BroadcastReceiver) {
        manager.unregisterReceiver(receiver)
    }

    fun send() {
        manager.sendBroadcast(Intent(ACTION_UPDATE))
    }
}
