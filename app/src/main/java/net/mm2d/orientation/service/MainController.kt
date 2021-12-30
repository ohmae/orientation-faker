/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository

@SuppressLint("StaticFieldLeak")
object MainController {
    private lateinit var context: Context
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(c: Context) {
        context = c.applicationContext
        scope.launch {
            PreferenceRepository.get()
                .orientationPreferenceRepository
                .flow
                .collect {
                    if (it.enabled && !MainService.isStarted) {
                        MainService.start(context)
                    }
                }
        }
    }
}
