package app.cinemagold.model.user

import app.cinemagold.model.generic.IdAndName

data class Profile (
    val id: Int?,
    var avatar: IdAndName,
    var name: String
)