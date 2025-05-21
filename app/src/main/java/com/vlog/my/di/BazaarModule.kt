package com.vlog.my.di

import com.vlog.my.data.bazaar.BazaarScriptsDao
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.data.bazaar.BazaarScriptsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BazaarModule {
    
    @Provides
    @Singleton
    fun provideBazaarScriptsRepository(
        bazaarScriptsDao: BazaarScriptsDao,
        bazaarScriptsService: BazaarScriptsService
    ): BazaarScriptsRepository {
        return BazaarScriptsRepository(bazaarScriptsDao, bazaarScriptsService)
    }
}