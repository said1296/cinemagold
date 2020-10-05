package app.cinemagold.model.content

import app.cinemagold.BuildConfig

data class Episode (
    val id : Int = -1,
    val description : String = "",
    var length : String = "",
    val name : String = "",
    val number : String = "",
    val src : String = "",
    var thumbnailSrc : String = BuildConfig.LOGO_URL,
    val subtitles : List<Subtitle> = emptyList()
){
    init {
        //Handle if asset is in local server
        if(!thumbnailSrc.startsWith("http")){
            this.thumbnailSrc = BuildConfig.THUMBNAIL_REMOTE + thumbnailSrc
        }

        //Set length units
        this.length = "$length min"
    }
}
