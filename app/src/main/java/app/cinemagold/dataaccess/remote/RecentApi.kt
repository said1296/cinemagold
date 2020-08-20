package app.cinemagold.dataaccess.remote

import app.cinemagold.model.content.Recent
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RecentApi {
    @GET("recent")
    suspend fun get(@Query("profileId") profileId: Int) : NetworkResponse<MutableList<Recent>, NetworkError>

    @POST("recent")
    suspend fun saveRecent(@Body recent: Recent) : NetworkResponse<Boolean, NetworkError>
}