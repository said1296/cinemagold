package app.cinemagold.model.user

data class RegisterForm (
    var countryId : Int? = null,
    var lastname : String? = null,
    var mail : String? = null,
    var name : String? = null,
    var password : String? = null,
    var phone : String? = null,
    var sellerId: String? = null
)
