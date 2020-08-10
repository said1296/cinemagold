package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.generic.LongIdAndName
import app.cinemagold.model.network.NetworkError
import app.cinemagold.model.network.Response
import app.cinemagold.model.user.RegisterForm
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.*

interface AuthenticationApi {
    @POST("authenticate")
    @Multipart
    suspend fun authenticate(@Query("email") email : String,
                             @Query("password") password : String,
                             @Part("remember") remember: Boolean = true) : NetworkResponse<Response, NetworkError>

    @GET("authenticate/check")
    suspend fun isAuthenticated() : NetworkResponse<Response, NetworkError>

    @POST("user")
    suspend fun register(@Body registerForm: RegisterForm) :  NetworkResponse<Response, NetworkError>

    @DELETE("authenticate/deauth")
    suspend fun deauthDevice(@Query("tokenId") id : Long) : NetworkResponse<Response, NetworkError>

    @GET("authenticate/device")
    suspend fun getDevices() : NetworkResponse<List<LongIdAndName>, NetworkError>
}