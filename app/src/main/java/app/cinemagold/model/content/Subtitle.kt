package app.cinemagold.model.content

import app.cinemagold.model.generic.IdAndName

data class Subtitle(
    val src : String = "",
    val language : IdAndName,
    val subtitleType : IdAndName
)