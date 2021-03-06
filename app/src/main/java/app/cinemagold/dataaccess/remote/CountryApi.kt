package app.cinemagold.dataaccess.remote

import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.network.NetworkError
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET

interface CountryApi {
    @GET("country")
    suspend fun getAll() : NetworkResponse<MutableList<IdAndName>, NetworkError>
}