/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(
    @ApplicationContext context: Context,
    packagePreferenceRepository: PackagePreferenceRepository,
    val orientationPreferenceRepository: OrientationPreferenceRepository,
    val controlPreferenceRepository: ControlPreferenceRepository,
    val designPreferenceRepository: DesignPreferenceRepository,
    val menuPreferenceRepository: MenuPreferenceRepository,
    val reviewPreferenceRepository: ReviewPreferenceRepository,
) {
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val packagePreferenceFlow: Flow<PackagePreference> = packagePreferenceRepository.flow
    val orientationPreferenceFlow: Flow<OrientationPreference> = orientationPreferenceRepository.flow
    val controlPreferenceFlow: Flow<ControlPreference> = controlPreferenceRepository.flow
    val designPreferenceFlow: Flow<DesignPreference> = designPreferenceRepository.flow
    val menuPreferenceFlow: Flow<MenuPreference> = menuPreferenceRepository.flow
    val reviewPreferenceFlow: Flow<ReviewPreference> = reviewPreferenceRepository.flow

    init {
        scope.launch {
            menuPreferenceFlow.collect {
                AppCompatDelegate.setDefaultNightMode(it.nightMode)
            }
        }
        scope.launch(Dispatchers.IO) {
            packagePreferenceFlow.collect()
            OldPreference.deleteAll(context)
        }
    }

    private val powerPluggedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val manuallyOrientationFlow: Flow<OrientationRequest> =
        orientationPreferenceRepository.manuallyOrientationFlow
    private val packageOrientationFlow: MutableStateFlow<OrientationRequest> = MutableStateFlow(OrientationRequest())

    private val powerOrientationFlow: Flow<OrientationRequest> = combine(
        orientationPreferenceFlow
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

    val actualOrientationPreferenceFlow = combine(
        orientationPreferenceFlow,
        preferredOrientationFlow
    ) { preferences, preferred ->
        if (!preferences.enabled || preferred == Orientation.INVALID) preferences else preferences.copy(orientation = preferred)
    }.shareIn(scope, SharingStarted.Eagerly, 1)
        .distinctUntilChanged()

    fun updatePackageOrientation(orientation: Orientation) {
        packageOrientationFlow.value = OrientationRequest(orientation)
    }

    fun updatePowerPlugged(plugged: Boolean) {
        powerPluggedFlow.value = plugged
    }
}
