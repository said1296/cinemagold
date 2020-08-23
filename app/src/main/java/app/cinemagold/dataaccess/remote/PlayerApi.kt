package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface PlayerApi {
    @GET("player/authorize")
    suspend fun authorize(@Query("remaining") remaining: Long,
                          @Query("mediaTypeId") contentTypeId: Int,
                          @Query("isMobileApp") isMobileApp : Boolean = true) : NetworkResponse<IdAndName, NetworkError>

    @DELETE("player/authorize")
    suspend fun deleteAuthorization() : NetworkResponse<Boolean, NetworkError>
}