package app.cinemagold.model.user

data class User(val name: String,
                val id: Int = 12,
                val token : String = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsb2dpbiIsImV4cCI6MTYyNDU1MjUyNywidXNlciI6ImNlc2FyLmNhcmRvem83NUBnbWFpbC5jb20iLCJ1c2VySWQiOjEyLCJpYXQiOjE1OTM0NDg1Mjd9.rkJeUPS6uYcjNAVqUs_YpxKsW8WNrtWemAjGYVWpQPVvBOgocpCegIWlOabcbYWGh6mcv1AMIVfMn3TjOf0f3w"
)