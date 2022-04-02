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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
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
    @ApplicationContext context: Context
) {
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val packagePreferenceRepository = PackagePreferenceRepository(context)
    val orientationPreferenceRepository = OrientationPreferenceRepository(context)
    val controlPreferenceRepository = ControlPreferenceRepository(context)
    val designPreferenceRepository = DesignPreferenceRepository(context)
    val menuPreferenceRepository = MenuPreferenceRepository(context)
    val reviewPreferenceRepository = ReviewPreferenceRepository(context)
    private val packagePreferenceFlow: SharedFlow<PackagePreference>
    val orientationPreferenceFlow: SharedFlow<OrientationPreference>
    val controlPreferenceFlow: SharedFlow<ControlPreference>
    val designPreferenceFlow: SharedFlow<DesignPreference>
    val menuPreferenceFlow: SharedFlow<MenuPreference>
    val reviewPreferenceFlow: SharedFlow<ReviewPreference>

    init {
        Default.initialize(context)
        packagePreferenceFlow = packagePreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
        orientationPreferenceFlow = orientationPreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
        controlPreferenceFlow = controlPreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
        designPreferenceFlow = designPreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
        menuPreferenceFlow = menuPreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)
        reviewPreferenceFlow = reviewPreferenceRepository.flow
            .shareIn(scope, replay = 1, started = SharingStarted.Eagerly)

        scope.launch {
            menuPreferenceFlow.collect {
                AppCompatDelegate.setDefaultNightMode(it.nightMode)
            }
        }
        scope.launch(Dispatchers.IO) {
            delay(1000)
            OldPreference(context).deleteIfEmpty()
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
