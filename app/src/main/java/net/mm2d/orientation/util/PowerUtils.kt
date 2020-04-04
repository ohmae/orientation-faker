package net.mm2d.orientation.util

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

object PowerUtils {
    fun isInteractive(context: Context): Boolean =
        context.getSystemService<PowerManager>()?.isInteractive == true
}
