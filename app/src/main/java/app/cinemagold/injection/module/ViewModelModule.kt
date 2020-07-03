package app.cinemagold.injection.module

import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.ContentTypeApi
import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.ui.home.HomeViewModel
import app.cinemagold.ui.preview.PreviewViewModel
import app.cinemagold.ui.search.SearchViewModel
import app.cinemagold.ui.serialized.MovieViewModel
import app.cinemagold.ui.serialized.SerializedViewModel
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
}