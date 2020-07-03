package app.cinemagold.dataaccess.remote

import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ContentApi {
    @GET("content/new")
    suspend fun getNew(@Query("ct") limit : Int) : NetworkResponse<MutableList<Content>, NetworkError>

    @GET("content/genre/group")
    suspend fun getGroupedByGenre() : NetworkResponse<MutableList<ContentGroupedByGenre>, NetworkError>

    @GET("content/genre/group/contentType")
    suspend fun getByContentTypeIdAndGroupedByGenre(@Query("contentTypeId") contentTypeId : Int) : NetworkResponse<MutableList<ContentGroupedByGenre>, NetworkError>

    @GET("content/serialized/genre/group/")
    suspend fun getSerializedGroupedByGenre() : NetworkResponse<MutableList<ContentGroupedByGenre>, NetworkError>

    @GET("content/movie")
    suspend fun getMovie(@Query("id") contentId : Int) : NetworkResponse<Content, NetworkError>

    @GET("content/serie")
    suspend fun getSerialized(@Query("id") contentId : Int) : NetworkResponse<Content, NetworkError>

    @GET("content/serialized/genre")
    suspend fun getSerializedByGenre(@Query("genreId") genreId : Int, @Query("max") max : Int) : NetworkResponse<MutableList<Content>, NetworkError>

    @GET("content/genre/media_type")
    suspend fun getByGenreIdAndOptionalContentTypeId(@Query("genreId") genreId : Int, @Query("mediaTypeId") contentTypeId : Int?, @Query("max") max : Int) : NetworkResponse<MutableList<Content>, NetworkError>

    @GET("/search/name")
    suspend fun searchByName(@Query("name") name : String, @Query("max") max : Int) : NetworkResponse<MutableList<Content>, NetworkError>
}