package app.cinemagold.model.content

import app.cinemagold.BuildConfig
import app.cinemagold.model.generic.IdAndName

data class Recent (
    val contentId : Int,
    val elapsed: Int,
    val elapsedPercent: Float,
    val profileId: Int? = null,
    val episodeId : Int? = null,
    val mediaType : IdAndName = IdAndName(),
    val length : Int = -1,
    val name: String = "",
    var sliderSrc: String = "",
    val genreMain: IdAndName = IdAndName(),
    val genreSecondary: IdAndName = IdAndName(),
    val seasonNumber : Int? = null,
    val seasonId: Int? = null,
    val episode: Episode? = null
){
    init {
        //Handle if asset is in local server
        if(!sliderSrc.startsWith("http")){
            this.sliderSrc = BuildConfig.SLIDER_REMOTE + sliderSrc
        }
    }
}