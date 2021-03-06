package app.cinemagold.injection.module

import android.content.Context
import app.cinemagold.dataaccess.remote.*
import app.cinemagold.ui.authentication.login.LoginViewModel
import app.cinemagold.ui.authentication.register.RegisterViewModel
import app.cinemagold.ui.browse.MenuViewModel
import app.cinemagold.ui.browse.home.HomeViewModel
import app.cinemagold.ui.browse.movie.MovieViewModel
import app.cinemagold.ui.browse.preview.PreviewViewModel
import app.cinemagold.ui.browse.search.SearchViewModel
import app.cinemagold.ui.browse.serialized.SerializedViewModel
import app.cinemagold.ui.option.notification.NotificationViewModel
import app.cinemagold.ui.option.profile.ProfileViewModel
import app.cinemagold.ui.option.profilecreate.AvatarGridViewModel
import app.cinemagold.ui.option.profilecreate.ProfileCreateViewModel
import app.cinemagold.ui.player.PlayerViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    fun provideHomeViewModel(contentApi : ContentApi, recentApi: RecentApi, playerApi: PlayerApi, context: Context) : HomeViewModel {
        return HomeViewModel(contentApi, recentApi, playerApi, context)
    }

    @Provides
    @Singleton
    fun providePreviewViewModel(contentApi : ContentApi, context: Context) : PreviewViewModel {
        return PreviewViewModel(contentApi, context)
    }

    @Provides
    fun provideSerializedViewModel(contentApi : ContentApi, genreApi : GenreApi, contentTypeApi: ContentTypeApi, context: Context) : SerializedViewModel {
        return SerializedViewModel(contentApi, genreApi, contentTypeApi, context)
    }

    @Provides
    @Singleton
    fun provideMovieViewModel(contentApi : ContentApi, genreApi : GenreApi, context: Context) : MovieViewModel {
        return MovieViewModel(contentApi, genreApi, context)
    }

    @Provides
    @Singleton
    fun provideSearchViewModel(contentApi : ContentApi, context: Context) : SearchViewModel {
        return SearchViewModel(contentApi, context)
    }

    @Provides
    @Singleton
    fun provideLoginViewModel(authenticationApi: AuthenticationApi) : LoginViewModel {
        return LoginViewModel(authenticationApi)
    }

    @Provides
    @Singleton
    fun provideRegisterViewModel(authenticationApi: AuthenticationApi, countryApi: CountryApi) : RegisterViewModel {
        return RegisterViewModel(authenticationApi, countryApi)
    }

    @Provides
    @Singleton
    fun provideProfileViewModel(profileApi: ProfileApi, context: Context) : ProfileViewModel {
        return ProfileViewModel(profileApi, context)
    }

    @Provides
    @Singleton
    fun provideProfileCreateViewModel(profileApi: ProfileApi, context: Context): ProfileCreateViewModel {
        return ProfileCreateViewModel(profileApi, context)
    }

    @Provides
    @Singleton
    fun provideMenuViewModel(authenticationApi: AuthenticationApi, profileApi: ProfileApi, notificationApi: NotificationApi, context: Context) : MenuViewModel{
        return MenuViewModel(authenticationApi, profileApi, notificationApi, context)
    }

    @Provides
    @Singleton
    fun providePlayerViewModel(recentApi: RecentApi, playerApi: PlayerApi, context: Context) : PlayerViewModel {
        return PlayerViewModel(recentApi, playerApi, context)
    }

    @Provides
    @Singleton
    fun provideAvatarGridViewModel(profileApi: ProfileApi) : AvatarGridViewModel {
        return AvatarGridViewModel(profileApi)
    }

    @Provides
    @Singleton
    fun provideNotificationViewModel(notificationApi: NotificationApi) : NotificationViewModel {
        return NotificationViewModel(notificationApi)
    }
}
