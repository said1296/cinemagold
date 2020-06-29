package app.cinemagold.model.content

data class ContentGroupedByGenre (
    val name : String = "",
    val id : Int,
    val contents : List<Content>
)