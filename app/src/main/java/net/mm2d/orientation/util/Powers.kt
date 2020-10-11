package net.mm2d.orientation.util

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

object Powers {
    fun isInteractive(context: Context): Boolean =
        context.getSystemService<PowerManager>()?.isInteractive == true
}
