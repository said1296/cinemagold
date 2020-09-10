package app.cinemagold.ui.browse.movie

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch


class MovieViewModel(private val contentApi : ContentApi, private val genreApi : GenreApi, val context : Context)  : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val contentGroupedByGenre : MutableLiveData<MutableList<ContentGroupedByGenre>> by lazy {
        MutableLiveData<MutableList<ContentGroupedByGenre>>(mutableListOf())
    }
    val genres : MutableLiveData<MutableList<IdAndName>> by lazy {
        MutableLiveData<MutableList<IdAndName>>()
    }
    val contentGenre : LiveEvent<MutableList<Content>> by lazy {
        LiveEvent<MutableList<Content>>()
    }
    var currentGenre = IdAndName()
    private val contentTypeId = ContentType.MOVIE.value
    var isKids = false

    fun initialize(){
        setIsKids()
        requestContentGroupedByGenre()
        requestGenres()
    }


    //Events
    fun selectedGenre(genre : IdAndName){
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
        currentGenre = IdAndName()
    }


    //Requests
    private fun requestContentGroupedByGenre(){
        viewModelScope.launch {
            when(val response = contentApi.getByContentTypeIdAndGroupedByGenre(contentTypeId, isKids)){
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
            val response = contentApi.getByGenreIdAndOptionalContentTypeId(currentGenre.id, contentTypeId, 60, isKids)
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
                    response.body.add(0, IdAndName(-1, "Todos"))
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

    //Action
    private fun setIsKids(){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
        isKids = currentProfile.id == -1
    }
}
