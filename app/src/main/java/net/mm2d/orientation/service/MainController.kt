/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object MainController {
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    fun update() {
        MainService.update(context)
    }

    fun start() {
        MainService.start(context)
    }

    fun stop() {
        MainService.stop(context)
    }
}
