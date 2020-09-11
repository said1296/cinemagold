package app.cinemagold.injection.module

import android.content.Context
import app.cinemagold.ui.browse.common.recycleradapter.ContentGridRVA
import app.cinemagold.ui.browse.common.recycleradapter.ContentHorizontalRVA
import app.cinemagold.ui.browse.home.ContentVerticalRVA
import app.cinemagold.ui.browse.home.ContentRecentRVA
import app.cinemagold.ui.browse.preview.EpisodeRVA
import app.cinemagold.ui.browse.serialized.GenreRVA
import app.cinemagold.ui.option.profilecreate.AvatarGridRVA
import app.cinemagold.ui.player.PlayerSelectorRVA
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

    @Provides
    @Singleton
    fun providePlayerSelectorRVA(context : Context) : PlayerSelectorRVA {
        return PlayerSelectorRVA(emptyList(), context)
    }

    @Provides
    fun provideContentRecentRVA(context: Context, picasso: Picasso) : ContentRecentRVA {
        return ContentRecentRVA(emptyList(), context, picasso)
    }

    @Provides
    @Singleton
    fun provideAvatarGridRVA(context: Context, picasso: Picasso) : AvatarGridRVA {
        return AvatarGridRVA(emptyList(), context, picasso)
    }
}
