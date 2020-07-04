package app.cinemagold.injection.component

import app.cinemagold.injection.module.*
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.player.PlayerActivity
import app.cinemagold.ui.browse.common.fragment.ContentGridFragment
import app.cinemagold.ui.browse.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.browse.home.HomeFragment
import app.cinemagold.ui.browse.movie.MovieFragment
import app.cinemagold.ui.browse.preview.PreviewFragment
import app.cinemagold.ui.browse.search.SearchFragment
import app.cinemagold.ui.browse.serialized.SerializedFragment
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
}