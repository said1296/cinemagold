package app.cinemagold.model.user

data class User(val name: String,
                val id: Int = 12,
                val token : String = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsb2dpbiIsImV4cCI6MTYyNDk4MTkzNiwidXNlciI6ImNlc2FyLmNhcmRvem83NUBnbWFpbC5jb20iLCJ1c2VySWQiOjEyLCJpYXQiOjE1OTM4Nzc5MzZ9.WqapeKGKx3SyuOQM1BA9leHXS8-tVwG87TJL4GA0IK5HPDBAGMpABiXOXoF9MZOjMwVnB4s3PMhZ17b9yh4N_w"
)