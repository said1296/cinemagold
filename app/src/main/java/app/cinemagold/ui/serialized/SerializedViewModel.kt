package app.cinemagold.ui.serialized

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

class SerializedViewModel(private val contentApi : ContentApi, private val genreApi : GenreApi, private val contentTypeApi: ContentTypeApi)  : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val contentGroupedByGenre : MutableLiveData<MutableList<ContentGroupedByGenre>> by lazy {
        MutableLiveData<MutableList<ContentGroupedByGenre>>()
    }
    val genres : MutableLiveData<MutableList<GenericIdAndName>> by lazy {
        MutableLiveData<MutableList<GenericIdAndName>>()
    }
    val contentGenre : LiveEvent<MutableList<Content>> by lazy {
        LiveEvent<MutableList<Content>>()
    }
    val contentTypes : MutableLiveData<MutableList<GenericIdAndName>> by lazy {
        MutableLiveData<MutableList<GenericIdAndName>>()
    }
    var currentGenre = GenericIdAndName()
    var contentTypeSpinnerItems = mutableListOf<String>()
    var currentContentType = GenericIdAndName()

    init {
        requestContentTypesAndContentTypeSpinnerItems()
        requestContentGroupedByGenre()
        requestGenres()
    }

    //Events
    fun selectedContentType(contentTypeIndex : Int){
        if(currentContentType != contentTypes.value!![contentTypeIndex]){
            currentContentType = contentTypes.value!![contentTypeIndex]
            requestGenres()
            if(currentGenre.id == -1){
                requestContentGroupedByGenre()
                requestContentGroupedByGenre()
                return
            }else{
                requestContentByGenreAndContentType()
            }
        }
    }

    fun selectedGenre(genre : GenericIdAndName){
        if(currentGenre!=genre){
            currentGenre=genre
            //If "All genres" selected
            if(currentGenre.id==-1){
                requestContentGroupedByGenre()
                return
            }
            requestContentByGenreAndContentType()
        }
    }

    fun stoppedFragment(){
        //Reinitialize genre value
        currentGenre = GenericIdAndName()
    }


    //Requests
    private fun requestContentGroupedByGenre(){
        viewModelScope.launch {
            println(currentContentType.id)
            val response =
                if(currentContentType.id == -1){
                    contentApi.getSerializedGroupedByGenre()
                } else{
                    contentApi.getByContentTypeIdAndGroupedByGenre(currentContentType.id)
                }
            when(response){
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

    private fun requestContentByGenreAndContentType(){
        viewModelScope.launch {
            val response =
                if(currentContentType.id==-1){
                    contentApi.getSerializedByGenre(currentGenre.id, 21)
                }else{
                    contentApi.getByGenreIdAndOptionalContentTypeId(currentGenre.id, currentContentType.id, 21)
                }
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
            val response =
                if(currentContentType.id == -1){
                    genreApi.getSerialized()
                }else{
                    genreApi.getByContentTypeId(currentContentType.id)
                }
            when(response){
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

    private fun requestContentTypesAndContentTypeSpinnerItems(){
        viewModelScope.launch {
            when(val data = contentTypeApi.getAll()){
                is NetworkResponse.Success -> {
                    data.body.removeAll { contentType -> contentType.id == ContentType.MOVIE.value}
                    data.body.add(0, GenericIdAndName(-1, "Programas de TV"))
                    //Set content type spinner items
                    for(contentType in data.body){
                        contentTypeSpinnerItems.add(contentType.name)
                    }
                    contentTypes.postValue(data.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(data.body?.status.toString() + " " + data.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(data.error.toString())
                    println(data.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }
}
