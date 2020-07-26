package app.cinemagold.model.content

enum class SubtitleType(val value: Int) {
    //Values need to be the same as on the remote database
    NORMAL (0),
    FORCED (1);

    companion object {
        //Get SubtitleType from value
        fun from(value: Int): SubtitleType = values().first { it.value == value }
    }
}