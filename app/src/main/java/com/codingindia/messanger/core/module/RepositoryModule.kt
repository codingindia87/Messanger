package com.codingindia.messanger.core.module

import com.codingindia.messanger.core.repository.PresenceRepository
import com.codingindia.messanger.core.repository.PresenceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPresenceRepository(
        presenceRepositoryImpl: PresenceRepositoryImpl
    ): PresenceRepository
}
