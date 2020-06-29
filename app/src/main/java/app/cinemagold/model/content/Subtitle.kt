package app.cinemagold.model.content

import app.cinemagold.model.generic.GenericIdAndName

data class Subtitle(
    val src : String = "",
    val language : GenericIdAndName,
    val subtitleType : GenericIdAndName
)