package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET

interface NotificationApi {
    @GET("notification")
    suspend fun getAll() : NetworkResponse<List<IdAndName>, NetworkError>
}