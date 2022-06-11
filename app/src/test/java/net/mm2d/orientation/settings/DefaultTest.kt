package net.mm2d.orientation.settings

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultTest {
    @Test
    fun getColor() {
        Default(ApplicationProvider.getApplicationContext())
    }
}
