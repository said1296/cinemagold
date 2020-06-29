package app.cinemagold.model.content

import app.cinemagold.BuildConfig
import app.cinemagold.model.generic.GenericIdAndName

data class Content(
    val id : Int = -1,
    val name: String = "",
    var sliderSrc : String = "",
    var posterSrc : String = "",
    var length : String = "-1",
    val description: String = "",
    val descriptionShort: String = "",
    val genreMain : GenericIdAndName = GenericIdAndName(),
    val genreSecondary : GenericIdAndName = GenericIdAndName(),
    val mediaType : GenericIdAndName = GenericIdAndName(),
    var score : Float = -1F,
    val movie : Movie = Movie(),
    val seasons : List<Season> = emptyList()
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
        if(mediaType.id==2){
            this.length = "$length min"
        }else{
            this.length = "$length temp"
        }

        //Set score base 5
        this.score = score/2
    }
}