package com.studieresan.studs.di

import com.studieresan.studs.data.StudsService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ServiceModule {

    @Provides
    @Singleton
    fun provideStudsService(retrofit: Retrofit): StudsService = retrofit.create(StudsService::class.java)
}
