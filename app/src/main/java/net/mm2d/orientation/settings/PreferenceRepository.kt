/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.OrientationHelper

class PreferenceRepository private constructor(context: Context) {
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    val packagePreferenceRepository = PackagePreferenceRepository(context)
    val controlPreferenceRepository = ControlPreferenceRepository(context)
    val designPreferenceRepository = DesignPreferenceRepository(context)
    val menuPreferenceRepository = MenuPreferenceRepository(context)
    val reviewPreferenceRepository = ReviewPreferenceRepository(context)

    init {
        scope.launch {
            packagePreferenceRepository.flow.collect()
        }
        scope.launch {
            controlPreferenceRepository.flow.collect()
        }
        scope.launch {
            designPreferenceRepository.flow.collect()
        }
        scope.launch {
            menuPreferenceRepository.flow.collect {
                AppCompatDelegate.setDefaultNightMode(it.nightMode)
                OrientationHelper.setWarnSystemRotate(it.warnSystemRotate)
            }
        }
        scope.launch {
            reviewPreferenceRepository.flow.collect()
        }
    }

    companion object {
        private lateinit var INSTANCE: PreferenceRepository

        fun initialize(context: Context) {
            INSTANCE = PreferenceRepository(context.applicationContext)
        }

        fun get(): PreferenceRepository = INSTANCE
    }
}
