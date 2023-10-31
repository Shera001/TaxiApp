package uz.test.taxiapp.di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.test.taxiapp.MainActivity

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    fun provideAppCompatActivity(mainActivity: MainActivity): AppCompatActivity
}