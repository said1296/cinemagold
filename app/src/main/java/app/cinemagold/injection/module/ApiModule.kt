package app.cinemagold.injection.module;


import app.cinemagold.dataaccess.remote.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun getGenreApi(retrofit: Retrofit): GenreApi {
        return retrofit.create(GenreApi::class.java)
    }
    @Provides
    @Singleton
    fun getContentAPi(retrofit: Retrofit): ContentApi {
        return retrofit.create(ContentApi::class.java)
    }
    @Provides
    @Singleton
    fun getContentTypeApi(retrofit: Retrofit): ContentTypeApi {
        return retrofit.create(ContentTypeApi::class.java)
    }

    @Provides
    @Singleton
    fun getAuthenticationApi(retrofit: Retrofit): AuthenticationApi {
        return retrofit.create(AuthenticationApi::class.java)
    }

    @Provides
    @Singleton
    fun getCountryApi(retrofit: Retrofit): CountryApi {
        return retrofit.create(CountryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRecentApi(retrofit: Retrofit): RecentApi{
        return retrofit.create(RecentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi{
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlayerApi(retrofit: Retrofit): PlayerApi {
        return retrofit.create(PlayerApi::class.java)
    }
}
