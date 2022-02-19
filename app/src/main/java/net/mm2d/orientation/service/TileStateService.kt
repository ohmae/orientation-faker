package net.mm2d.orientation.service

import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.coroutineScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@RequiresApi(VERSION_CODES.N)
@AndroidEntryPoint
class TileStateService : TileService(), LifecycleOwner {
    private val dispatcher = ServiceLifecycleDispatcher(this)
    override fun getLifecycle(): Lifecycle = dispatcher.lifecycle

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateState(MainService.isStarted)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateState(started: Boolean) {
        val qsTile = qsTile ?: return
        val newState = if (started) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (qsTile.state != newState) {
            qsTile.state = newState
            qsTile.updateTile()
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateState(MainService.isStarted)
    }

    override fun onClick() {
        lifecycle.coroutineScope.launch {
            preferenceRepository.orientationPreferenceRepository.toggleEnabled()
        }
    }
}
