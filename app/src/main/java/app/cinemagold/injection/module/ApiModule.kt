package app.cinemagold.injection.module;


import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.ContentTypeApi
import app.cinemagold.dataaccess.remote.GenreApi
import dagger.Module;
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
}