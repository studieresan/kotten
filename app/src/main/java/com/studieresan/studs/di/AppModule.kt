package com.studieresan.studs.di

import com.studieresan.studs.StudsApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: StudsApplication) {

    @Provides
    @Singleton
    fun provideApplication(): StudsApplication = application
}
