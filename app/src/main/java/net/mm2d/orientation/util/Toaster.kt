package net.mm2d.orientation.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object Toaster {
    fun showLong(context: Context, @StringRes stringRes: Int) {
        Toast.makeText(context.applicationContext, stringRes, Toast.LENGTH_LONG).show()
    }
}
