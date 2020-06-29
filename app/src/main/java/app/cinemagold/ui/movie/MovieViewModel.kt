package app.cinemagold.ui.serialized

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.ContentTypeApi
import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.generic.GenericIdAndName
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch


class MovieViewModel(private val contentApi : ContentApi, private val genreApi : GenreApi, private val contentTypeApi: ContentTypeApi)  : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val contentGroupedByGenre : MutableLiveData<MutableList<ContentGroupedByGenre>> by lazy {
        MutableLiveData<MutableList<ContentGroupedByGenre>>(mutableListOf())
    }
    val genres : MutableLiveData<MutableList<GenericIdAndName>> by lazy {
        MutableLiveData<MutableList<GenericIdAndName>>()
    }
    val contentGenre : LiveEvent<MutableList<Content>> by lazy {
        LiveEvent<MutableList<Content>>()
    }
    var currentGenre = GenericIdAndName()
    private val contentTypeId = ContentType.MOVIE.value

    init {
        requestContentGroupedByGenre()
        requestGenres()
    }


    //Events
    fun selectedGenre(genre : GenericIdAndName){
        if(currentGenre!=genre){
            currentGenre=genre
            //If "All genres" selected
            if(currentGenre.id==-1){
                requestContentGroupedByGenre()
                return
            }
            requestContentByGenre()
        }
    }

    //Reinitialize current values
    fun stoppedFragment(){
        //Set currentContentType to All
        currentGenre = GenericIdAndName()
    }


    //Requests
    private fun requestContentGroupedByGenre(){
        viewModelScope.launch {
            when(val response = contentApi.getByContentTypeIdAndGroupedByGenre(contentTypeId)){
                is NetworkResponse.Success -> {
                    contentGroupedByGenre.postValue(response.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(response.body?.status.toString() + " " + response.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(response.error.toString())
                    println(response.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }

    private fun requestContentByGenre(){
        viewModelScope.launch {
            val response = contentApi.getByGenreIdAndOptionalContentTypeId(currentGenre.id, contentTypeId, 21)
            println(response)
            when(response){
                is NetworkResponse.Success -> {
                    contentGenre.postValue(response.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(response.body?.status.toString() + " " + response.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(response.error.toString())
                    println(response.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }

    private fun requestGenres(){
        viewModelScope.launch {
            when(val response = genreApi.getByContentTypeId(contentTypeId)){
                is NetworkResponse.Success -> {
                    response.body.add(0, GenericIdAndName(-1, "Todos"))
                    genres.postValue(response.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(response.body?.status.toString() + " " + response.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(response.error.toString())
                    println(response.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }
}
