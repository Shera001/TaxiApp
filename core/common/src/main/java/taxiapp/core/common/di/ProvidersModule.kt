package taxiapp.core.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import taxiapp.core.common.util.AndroidResourceProvider
import taxiapp.core.common.util.ResourceProvider

@Module
@InstallIn(SingletonComponent::class)
object ProvidersModule {

    @Provides
    fun resourceProvide(
        @ApplicationContext context: Context
    ): ResourceProvider {
        return AndroidResourceProvider(context.resources)
    }
}