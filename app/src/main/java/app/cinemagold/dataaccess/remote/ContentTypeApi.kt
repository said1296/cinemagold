package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.GenericIdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET

interface ContentTypeApi {
    @GET("media_type")
    suspend fun getAll() : NetworkResponse<MutableList<GenericIdAndName>, NetworkError>
}