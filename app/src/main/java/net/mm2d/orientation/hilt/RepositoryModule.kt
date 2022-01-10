package net.mm2d.orientation.hilt

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun bindPreferenceRepository(
        @ApplicationContext context: Context
    ): PreferenceRepository = PreferenceRepository(context)
}
