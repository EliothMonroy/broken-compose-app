package com.interviewprep.brokencalc

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @MainDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @IoDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    // provide it explicitly so Hilt can find it
    @Provides
    fun provideSessionTracker(): SessionTracker = SessionTracker()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    abstract fun bindSettingsStore(impl: InMemorySettingsStore): SettingsStore
}

// lets classes that Hilt doesn't create grab dependencies
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun sessionTracker(): SessionTracker
    fun settingsStore(): SettingsStore
}

fun appEntryPoint(): AppEntryPoint {
    return EntryPointAccessors.fromApplication(MainActivity.instance.applicationContext, AppEntryPoint::class.java)
}
