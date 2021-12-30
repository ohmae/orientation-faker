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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper

class PreferenceRepository private constructor(context: Context) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val packagePreferenceRepository = PackagePreferenceRepository(context)
    val orientationPreferenceRepository = OrientationPreferenceRepository(context)
    val controlPreferenceRepository = ControlPreferenceRepository(context)
    val designPreferenceRepository = DesignPreferenceRepository(context)
    val menuPreferenceRepository = MenuPreferenceRepository(context)
    val reviewPreferenceRepository = ReviewPreferenceRepository(context)

    val preferredOrientationFlow: MutableStateFlow<Orientation> = MutableStateFlow(Orientation.INVALID)
    val orientationPreferenceFlow = combine(
        orientationPreferenceRepository.flow,
        preferredOrientationFlow
    ) { preferences, preferred ->
        val orientation = if (preferred == Orientation.INVALID) preferences.orientation else preferred
        preferences.copy(orientation = orientation)
    }

    init {
        scope.launch {
            packagePreferenceRepository.flow.collect()
        }
        scope.launch {
            orientationPreferenceRepository.flow.collect {
                preferredOrientationFlow.emit(it.orientation)
            }
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

    fun enableAndOrientation(orientation: Orientation) {
        scope.launch {
            orientationPreferenceRepository.updateEnabled(true)
            orientationPreferenceRepository.updateOrientation(orientation)
        }
    }

    fun adjustOrientation() {
        scope.launch {
            combine(
                orientationPreferenceRepository.flow,
                designPreferenceRepository.flow,
                ::Pair
            ).take(1).collect { (orientation, design) ->
                if (!design.orientations.contains(orientation.orientation)) {
                    orientationPreferenceRepository.updateOrientation(design.orientations[0])
                }
            }
        }
    }

    fun inclementCancelCount() {
        scope.launch {
            reviewPreferenceRepository.inclementCancelCount()
        }
    }

    fun updateReviewed(reviewed: Boolean) {
        scope.launch {
            reviewPreferenceRepository.updateReviewed(reviewed)
        }
    }

    fun updateReported(reported: Boolean) {
        scope.launch {
            reviewPreferenceRepository.updateReported(reported)
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
