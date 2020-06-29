package app.cinemagold.injection.module;

import app.cinemagold.model.user.User
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton

@Module
class ModelModule {
    @Provides @Singleton
    fun giveUser() : User {
        return User("Ian");
    }
}