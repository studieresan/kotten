package com.studieresan.studs.di

import com.studieresan.studs.StudsApplication
import com.studieresan.studs.data.AddJwtInterceptor
import com.studieresan.studs.data.ReceivedJwtInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val STUDS_URL = "https://studs-overlord.herokuapp.com"
private const val CACHE_SIZE: Long = 5 * 1024 * 1024

@Module
class NetModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(application: StudsApplication, cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(AddJwtInterceptor(application))
                .addInterceptor(ReceivedJwtInterceptor(application))
                .build()
    }

    @Provides
    @Singleton
    fun provideHttpCache(application: StudsApplication) = Cache(application.cacheDir, CACHE_SIZE)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(STUDS_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}
