package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.network.NetworkError
import app.cinemagold.model.network.Response
import app.cinemagold.model.user.Profile
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.*

interface ProfileApi {
    @GET("profile")
    suspend fun getProfiles() : NetworkResponse<MutableList<Profile>, NetworkError>

    @POST("profile")
    suspend fun createProfile(@Body profile: Profile): NetworkResponse<Profile, NetworkError>

    @DELETE("profile")
    suspend fun deleteProfile(@Query("id") id: Int): NetworkResponse<Response, NetworkError>

    @GET("profile/avatar")
    suspend fun getAvatars(): NetworkResponse<List<IdAndName>, NetworkError>
}