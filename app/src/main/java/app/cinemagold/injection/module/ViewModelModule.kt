package app.cinemagold.injection.module

import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.ContentTypeApi
import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.ui.authentication.login.LoginViewModel
import app.cinemagold.ui.browse.home.HomeViewModel
import app.cinemagold.ui.browse.preview.PreviewViewModel
import app.cinemagold.ui.browse.search.SearchViewModel
import app.cinemagold.ui.browse.serialized.MovieViewModel
import app.cinemagold.ui.browse.serialized.SerializedViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun provideHomeViewModel(contentApi : ContentApi) : HomeViewModel {
        return HomeViewModel(contentApi)
    }

    @Provides
    @Singleton
    fun providePreviewViewModel(contentApi : ContentApi) : PreviewViewModel {
        return PreviewViewModel(contentApi)
    }

    @Provides
    @Singleton
    fun provideSerializedViewModel(contentApi : ContentApi, genreApi : GenreApi, contentTypeApi: ContentTypeApi) : SerializedViewModel {
        return SerializedViewModel(contentApi, genreApi, contentTypeApi)
    }

    @Provides
    @Singleton
    fun provideMovieViewModel(contentApi : ContentApi, genreApi : GenreApi, contentTypeApi: ContentTypeApi) : MovieViewModel {
        return MovieViewModel(contentApi, genreApi, contentTypeApi)
    }

    @Provides
    @Singleton
    fun provideSearchViewModel(contentApi : ContentApi) : SearchViewModel {
        return SearchViewModel(contentApi)
    }

    @Provides
    @Singleton
    fun provideLoginViewModel(authenticationApi: AuthenticationApi) : LoginViewModel {
        return LoginViewModel(authenticationApi)
    }
}