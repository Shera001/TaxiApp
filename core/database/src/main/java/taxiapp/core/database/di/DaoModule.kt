package taxiapp.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import taxiapp.core.database.TaxiAppDatabase
import taxiapp.core.database.dao.LocationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    @Singleton
    fun provideLocationDao(database: TaxiAppDatabase): LocationDao = database.locationDao()
}