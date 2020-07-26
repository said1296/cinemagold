package app.cinemagold.model.user

data class User(val name: String,
                val id: Int = 12,
                val token : String = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsb2dpbiIsImV4cCI6MTU5NTY0ODk5MiwidXNlciI6ImNlc2FyLmNhcmRvem83NUBnbWFpbC5jb20iLCJ1c2VySWQiOjEyLCJpYXQiOjE1OTU2MzgxOTJ9.ju8Pni3P3-8xQf923o-s66xdSubG5CDkRh07b_9C5iZ-dgpIYdZqfLnCL7cZ5gY9ui9JGBaezND6_Pm2Fejikg"
)