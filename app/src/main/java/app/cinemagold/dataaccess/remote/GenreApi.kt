package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GenreApi {
    @GET("genre")
    suspend fun getAll() : NetworkResponse<List<IdAndName>, NetworkError>

    @GET("genre/serialized")
    suspend fun getSerialized() : NetworkResponse<MutableList<IdAndName>, NetworkError>

    @GET("genre/media_type")
    suspend fun getByContentTypeId(@Query("mediaTypeId") contentTypeId : Int) : NetworkResponse<MutableList<IdAndName>, NetworkError>
}