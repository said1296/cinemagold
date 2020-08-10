package app.cinemagold.injection.component

import app.cinemagold.injection.module.*
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.authentication.login.LoginFragment
import app.cinemagold.ui.authentication.register.RegisterFragment
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.fragment.ContentGridFragment
import app.cinemagold.ui.browse.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.browse.home.HomeFragment
import app.cinemagold.ui.browse.movie.MovieFragment
import app.cinemagold.ui.browse.preview.PreviewFragment
import app.cinemagold.ui.browse.search.SearchFragment
import app.cinemagold.ui.browse.serialized.SerializedFragment
import app.cinemagold.ui.option.profile.ProfileFragment
import app.cinemagold.ui.option.profilecreate.AvatarGridFragment
import app.cinemagold.ui.option.profilecreate.ProfileCreateFragment
import app.cinemagold.ui.player.PlayerActivity
import dagger.Component
import javax.inject.Singleton


//Application-wide injectable dependencies
@Singleton
@Component(modules = [
    NetworkModule::class,
    ApiModule::class,
    ModelModule::class,
    RVAModule::class,
    ApplicationModule::class,
    MediaModule::class,
    FragmentModule::class,
    ViewModelModule::class
])
interface ApplicationComponent {
    fun inject(activity: BrowseActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: PreviewFragment)
    fun inject(fragment: SerializedFragment)
    fun inject(fragment: MovieFragment)
    fun inject(fragment: SearchFragment)

    fun inject(fragment: ContentGroupedByGenreFragment)
    fun inject(fragment: ContentGridFragment)

    fun inject(activity: PlayerActivity)

    fun inject(activity: AuthenticationActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: RegisterFragment)

    fun inject(fragment: ProfileFragment)
    fun inject(fragment: ProfileCreateFragment)
    fun inject(fragment: AvatarGridFragment)
}