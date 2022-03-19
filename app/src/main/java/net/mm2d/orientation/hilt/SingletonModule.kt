package net.mm2d.orientation.hilt

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {
    @Provides
    @Singleton
    fun bindPreferenceRepository(
        @ApplicationContext context: Context
    ): PreferenceRepository = PreferenceRepository(context)

    @Provides
    @Singleton
    fun bindOrientationHelper(
        @ApplicationContext context: Context
    ): OrientationHelper = OrientationHelper(context)
}
