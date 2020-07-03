package app.cinemagold.injection.component

import app.cinemagold.injection.module.*
import app.cinemagold.ui.MainActivity
import app.cinemagold.ui.PlayerActivity
import app.cinemagold.ui.common.fragment.ContentGridFragment
import app.cinemagold.ui.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.home.HomeFragment
import app.cinemagold.ui.movie.MovieFragment
import app.cinemagold.ui.preview.PreviewFragment
import app.cinemagold.ui.search.SearchFragment
import app.cinemagold.ui.serialized.SerializedFragment
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
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: PreviewFragment)
    fun inject(fragment: SerializedFragment)
    fun inject(fragment: MovieFragment)
    fun inject(fragment: SearchFragment)

    fun inject(fragment: ContentGroupedByGenreFragment)
    fun inject(fragment: ContentGridFragment)

    fun inject(activity: PlayerActivity)
}