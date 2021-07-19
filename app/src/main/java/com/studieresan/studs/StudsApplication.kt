package com.studieresan.studs

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.studieresan.studs.di.*
import timber.log.Timber

class StudsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            // Plant Timber
            Timber.plant(Timber.DebugTree())
        }
        applicationComponent = createApplicationComponent()
    }

    private fun createApplicationComponent(): StudsApplicationComponent {
        return DaggerStudsApplicationComponent
                .builder()
                .appModule(AppModule(this))
                .netModule(NetModule())
                .serviceModule(ServiceModule())
                .build()
    }

    companion object {
        lateinit var applicationComponent: StudsApplicationComponent
            private set
    }
}
