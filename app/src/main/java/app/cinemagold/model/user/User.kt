package app.cinemagold.model.user

data class User(val name: String,
                val id: Int = 12,
                val token : String = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsb2dpbiIsImV4cCI6MTU5NDUxOTk3NywidXNlciI6ImNlc2FyLmNhcmRvem83NUBnbWFpbC5jb20iLCJ1c2VySWQiOjEyLCJpYXQiOjE1OTQ1MDkxNzd9.Y-n0k2i1Zkg_81gkS0ZNuEj85Uf0NT6NPB-ypxvdPK6r1yUpB8UZuXxGbjMU-5sWRODJ89Skk_DG2AWEKqrwlg"
)