package net.mm2d.orientation.util

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle.State

fun ComponentActivity.isActive(): Boolean =
    !isFinishing && lifecycle.currentState.isAtLeast(State.STARTED)
