package net.mm2d.orientation.util

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle.State

fun ComponentActivity.isResumed(): Boolean =
    lifecycle.currentState == State.RESUMED
