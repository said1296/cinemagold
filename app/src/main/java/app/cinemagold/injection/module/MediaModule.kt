package app.cinemagold.injection.module

import android.content.Context
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MediaModule {
    @Provides
    @Singleton
    fun providePicasso(context : Context) : Picasso {
        return Picasso.Builder(context).build()
    }
}