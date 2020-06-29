package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.GenericIdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GenreApi {
    @GET("genre")
    suspend fun getAll() : NetworkResponse<List<GenericIdAndName>, NetworkError>

    @GET("genre/serialized")
    suspend fun getSerialized() : NetworkResponse<MutableList<GenericIdAndName>, NetworkError>

    @GET("genre/media_type")
    suspend fun getByContentTypeId(@Query("mediaTypeId") contentTypeId : Int) : NetworkResponse<MutableList<GenericIdAndName>, NetworkError>
}