package app.cinemagold.model.content

data class Movie (
    val src: String = "",
    var director : String = "",
    var subtitles : List<Subtitle> = emptyList()
)