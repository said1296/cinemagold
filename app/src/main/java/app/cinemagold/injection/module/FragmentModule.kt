package app.cinemagold.injection.module

import app.cinemagold.ui.common.fragment.ContentGridFragment
import app.cinemagold.ui.common.fragment.ContentGroupedByGenreFragment
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FragmentModule {
    @Provides
    @Singleton
    fun provideContentGridFragment() : ContentGridFragment {
        return ContentGridFragment()
    }

    @Provides
    @Singleton
    fun provideContentGroupedByGenreFragment() : ContentGroupedByGenreFragment {
        return ContentGroupedByGenreFragment()
    }
}