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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper

class PreferenceRepository(context: Context) {
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val packagePreferenceRepository = PackagePreferenceRepository(context)
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
            .distinctUntilChangedBy { it.orientationWhenPowerIsConnected },
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
            packagePreferenceRepository.flow.take(1).collect()
        }
        scope.launch {
            orientationPreferenceRepository.flow.take(1).collect()
        }
        scope.launch {
            controlPreferenceRepository.flow.take(1).collect()
        }
        scope.launch {
            designPreferenceRepository.flow.take(1).collect()
        }
        scope.launch {
            menuPreferenceRepository.flow.collect {
                AppCompatDelegate.setDefaultNightMode(it.nightMode)
                OrientationHelper.setWarnSystemRotate(it.warnSystemRotate)
            }
        }
        scope.launch {
            reviewPreferenceRepository.flow.take(1).collect()
        }
        scope.launch(Dispatchers.IO) {
            delay(1000)
            OldPreference(context).deleteIfEmpty()
        }
    }

    suspend fun updatePackageOrientation(orientation: Orientation) {
        packageOrientationFlow.emit(OrientationRequest(orientation))
    }

    suspend fun updatePowerPlugged(plugged: Boolean) {
        powerPluggedFlow.emit(plugged)
    }
}
