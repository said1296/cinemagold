package app.cinemagold.model.network

data class Response (
    val status : Boolean,
    val error: List<String>?
)