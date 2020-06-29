package app.cinemagold.model.content

enum class ContentType(val value: Int) {
    //Values need to be the same as on the remote database
    NONE(-1),
    ANIME (1),
    MOVIE (2),
    SERIES (3),
    SOAP (4);

    companion object {
        //Get ContentType from value
        fun from(value: Int): ContentType = values().first { it.value == value }
    }
}