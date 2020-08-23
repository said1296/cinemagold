package app.cinemagold.model.user

enum class PlayerAuthorization(val value: Int) {
    //Values need to be the same as on the remote database
    AUTHORIZED(0),
    SUSPENDED (1),
    MEMBERSHIP_EXPIRED (2),
    DEVICE_LIMIT_REACHED (3),
    CONTENT_TYPE_PERMISSION_DENIED (4);

    companion object {
        //Get ContentType from value
        fun from(value: Int): PlayerAuthorization = values().first { it.value == value }
    }
}