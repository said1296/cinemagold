package app.cinemagold.model.content

import app.cinemagold.BuildConfig

data class Season (
    val number : Int = -1,
    val year : Int = -1,
    var length : String = "",
    var sliderSrc : String = "",
    var posterSrc : String = "",
    val episodes : List<Episode> = emptyList()
){
    init {
        //Handle if asset is in local server
        if(!sliderSrc.startsWith("http")){
            this.sliderSrc = BuildConfig.SLIDER_REMOTE + sliderSrc
        }

        //Set length units
        this.length = "$length caps."
    }
}