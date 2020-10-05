package app.cinemagold.model.content

import app.cinemagold.BuildConfig

data class Season (
    val id : Int = -1,
    val number : Int = -1,
    val year : Int = -1,
    var length : String = "",
    var sliderSrc : String = BuildConfig.LOGO_URL,
    var posterSrc : String = BuildConfig.LOGO_URL,
    val episodes : List<Episode> = emptyList()
){
    init {
        //Handle if asset is in local server
        if(!sliderSrc.startsWith("http")){
            this.sliderSrc = BuildConfig.SLIDER_REMOTE + sliderSrc
        }
        if(!posterSrc.startsWith("http")){
            this.posterSrc = BuildConfig.POSTER_REMOTE + posterSrc
        }


        //Set length units
        this.length = "$length caps."
    }
}
