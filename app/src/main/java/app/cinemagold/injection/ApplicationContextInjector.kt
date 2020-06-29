package app.cinemagold.injection

import android.app.Application
import app.cinemagold.injection.component.ApplicationComponent
import app.cinemagold.injection.component.DaggerApplicationComponent
import app.cinemagold.injection.module.ApplicationModule

class ApplicationContextInjector : Application() {
    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = buildComponent()
    }

    fun buildComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}