package taxiapp.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import taxiapp.core.data.repository.HomeRepositoryImpl
import taxiapp.core.domain.repository.HomeRepository

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindHomeRepository(repositoryImpl: HomeRepositoryImpl): HomeRepository
}