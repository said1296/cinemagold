package app.cinemagold.dataaccess.remote

import app.cinemagold.model.content.Content
import app.cinemagold.model.network.NetworkError
import app.cinemagold.model.network.Response
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.Field
import retrofit2.http.POST

interface AuthenticationApi {
    @POST("/authentication/login")
    suspend fun authenticate(  @Field("email") email : String,
                        @Field("password") password : String,
                        @Field("remember") remember: Boolean = false) : NetworkResponse<Response, NetworkError>
}