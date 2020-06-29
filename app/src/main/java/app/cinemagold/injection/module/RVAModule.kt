package app.cinemagold.injection.module

import android.content.Context
import app.cinemagold.ui.common.recycleradapter.ContentGridRVA
import app.cinemagold.ui.common.recycleradapter.ContentHorizontalRVA
import app.cinemagold.ui.common.recycleradapter.ContentVerticalRVA
import app.cinemagold.ui.preview.EpisodeRVA
import app.cinemagold.ui.serialized.GenreRVA
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RVAModule {

    @Provides
    fun provideContentHorizontalRVA(context : Context, picasso : Picasso) : ContentHorizontalRVA {
        return ContentHorizontalRVA(emptyList(), context, picasso)
    }

    @Provides
    @Singleton
    fun provideContentVerticalRVA(context : Context, picasso : Picasso) : ContentVerticalRVA {
        return ContentVerticalRVA(emptyList(), context, picasso)
    }

    @Provides
    @Singleton
    fun provideContentGridRVA(context : Context, picasso: Picasso) : ContentGridRVA {
        return ContentGridRVA(emptyList(), context, picasso)
    }

    @Provides
    @Singleton
    fun provideEpisodeRVA(context : Context, picasso : Picasso) : EpisodeRVA {
        return EpisodeRVA(emptyList(), context, picasso)
    }

    @Provides
    @Singleton
    fun provideGenreRVA(context : Context) : GenreRVA {
        return GenreRVA(emptyList(), context)
    }
}