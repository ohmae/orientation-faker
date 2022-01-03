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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
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

    private val powerPluggedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val manuallyOrientationFlow: Flow<OrientationRequest> =
        orientationPreferenceRepository.manuallyOrientationFlow
    private val packageOrientationFlow: MutableStateFlow<OrientationRequest> = MutableStateFlow(OrientationRequest())
    private val powerOrientationFlow: Flow<OrientationRequest> = combine(
        orientationPreferenceRepository.flow
            .distinctUntilChanged { old, new -> old.orientationWhenPowerIsConnected == new.orientationWhenPowerIsConnected },
        powerPluggedFlow
    ) { orientation, plugged ->
        OrientationRequest(if (plugged) orientation.orientationWhenPowerIsConnected else Orientation.INVALID)
    }
    private val preferredOrientationFlow: Flow<Orientation> = combine(
        manuallyOrientationFlow,
        packageOrientationFlow,
        powerOrientationFlow,
    ) { o1, o2, o3 ->
        listOf(o1, o2, o3)
            .sortedByDescending { it.timestamp }
            .firstOrNull { it.orientation != Orientation.INVALID }
            ?.orientation ?: Orientation.INVALID
    }.shareIn(scope, SharingStarted.Eagerly, 1)
        .distinctUntilChanged()

    val orientationPreferenceFlow = combine(
        orientationPreferenceRepository.flow,
        preferredOrientationFlow
    ) { preferences, preferred ->
        if (!preferences.enabled || preferred == Orientation.INVALID) preferences else preferences.copy(orientation = preferred)
    }.shareIn(scope, SharingStarted.Eagerly, 1)
        .distinctUntilChanged()

    init {
        scope.launch {
            packagePreferenceRepository.flow.collect()
        }
        scope.launch {
            orientationPreferenceRepository.flow.collect()
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

    fun updateOrientationManually(orientation: Orientation) {
        scope.launch {
            orientationPreferenceRepository.updateOrientationManually(orientation)
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

    fun updatePackageOrientation(orientation: Orientation) {
        scope.launch {
            packageOrientationFlow.emit(OrientationRequest(orientation))
        }
    }

    fun updatePowerPlugged(plugged: Boolean) {
        scope.launch {
            powerPluggedFlow.emit(plugged)
        }
    }

    companion object {
        private lateinit var INSTANCE: PreferenceRepository

        fun initialize(context: Context) {
            Default.initialize(context)
            INSTANCE = PreferenceRepository(context.applicationContext)
        }

        fun get(): PreferenceRepository = INSTANCE
    }
}
