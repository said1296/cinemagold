package app.cinemagold.injection

import android.app.Application
import app.cinemagold.injection.component.ApplicationComponent
import app.cinemagold.injection.component.DaggerApplicationComponent
import app.cinemagold.injection.module.ApplicationModule
import com.google.firebase.messaging.FirebaseMessaging


class ApplicationContextInjector : Application() {
    lateinit var applicationComponent: ApplicationComponent
    private val mainTopic = "cinemagold"

    override fun onCreate() {
        applicationComponent = buildComponent()
        FirebaseMessaging.getInstance().subscribeToTopic(mainTopic)
        super.onCreate()
    }

    private fun buildComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
