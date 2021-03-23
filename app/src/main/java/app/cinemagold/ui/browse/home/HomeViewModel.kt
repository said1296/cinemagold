package app.cinemagold.ui.browse.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.PlayerApi
import app.cinemagold.dataaccess.remote.RecentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.Recent
import app.cinemagold.model.network.NetworkError
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeViewModel(val contentApi : ContentApi, private val recentApi: RecentApi, val playerApi: PlayerApi, val context: Context)  : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var currentProfile : Profile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)

    val contentPremiere : LiveData<MutableList<Content>> = liveData(Dispatchers.IO) {
        when(val data = contentApi.getNew(8, isKids)){
            is NetworkResponse.Success -> {
                emit(data.body)
            }
            is NetworkResponse.ServerError -> {
                error.postValue(data.body?.status.toString() + " " + data.body?.message)
            }
            is NetworkResponse.NetworkError -> {
                error.postValue(data.error.toString())
            }
            else -> error.postValue("Unknown error")
        }
    }

    val contentGroupedByGenre : MutableLiveData<MutableList<ContentGroupedByGenre>> by lazy {
        MutableLiveData<MutableList<ContentGroupedByGenre>>()
    }


    val contentRecent : MutableLiveData<MutableList<Recent>> by lazy {
        MutableLiveData<MutableList<Recent>>()
    }

    val contentRecentSelected : LiveEvent<Content> by lazy {
        LiveEvent<Content>()
    }
    var seasonSelectedIndex = 0
    var episodeSelectedIndex = 0
    lateinit var recentSelected : Recent
    var isKids = false

    init {
        setIsKids()
    }

    //Events
    fun startedFragment(){
        if(!isKids){
            requestRecent()
        }
        if(contentGroupedByGenre.value == null || contentGroupedByGenre.value?.size==0){
            requestContentGroupedByGenre()
        }
        requestPurgeOfPlayerAuthorizations()
    }

    //ids = [contentId, seasonId, episodeId]
    fun clickedRecent(recent : Recent){
        val ids = mutableListOf<Int>()
        ids.add(recent.contentId)
        if(recent.seasonId!=null){
            ids.add(recent.seasonId)
            ids.add(recent.episode!!.id)
        }
        recentSelected = recent
        requestContent(ids)
    }

    //Requests

    private fun requestPurgeOfPlayerAuthorizations() {
        viewModelScope.launch {
            when(val data = playerApi.purgeAuthorizations()){
                is NetworkResponse.Success -> {}
                is NetworkResponse.ServerError -> { error.postValue(data.body?.status.toString() + " " + data.body?.message) }
                is NetworkResponse.NetworkError -> { error.postValue(data.error.toString()) }
                else -> error.postValue("Unknown error")
            }
        }
    }


    private fun requestContentGroupedByGenre(){
        viewModelScope.launch {
            when(val data = contentApi.getGroupedByGenre(isKids)){
                is NetworkResponse.Success -> {
                    contentGroupedByGenre.postValue(data.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(data.body?.status.toString() + " " + data.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(data.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }

    private fun requestRecent(){
        viewModelScope.launch {
            when(val data = recentApi.get(currentProfile.id!!)){
                is NetworkResponse.Success -> {
                    contentRecent.postValue(data.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(data.body?.status.toString() + " " + data.body?.message)
                    println(data.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(data.error.toString())
                    println(data.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }

    private fun requestContent(ids : List<Int>){
        viewModelScope.launch {
            val response : NetworkResponse<Content, NetworkError> =
                if(recentSelected.mediaType.id== ContentType.MOVIE.value){
                    contentApi.getMovie(ids[0])
                }else{
                    contentApi.getSerialized(ids[0], currentProfile.id!!)
                }

            when(response){
                is NetworkResponse.Success -> {
                    val content = response.body
                    if(recentSelected.mediaType.id!= ContentType.MOVIE.value){
                        //Find position of season and episode
                        for((seasonIndex, season) in content.seasons.withIndex()){
                            if(season.id==ids[1]){
                                seasonSelectedIndex = seasonIndex
                                for((episodeIndex, episode) in season.episodes.withIndex()){
                                    if(episode.id == ids[2]){
                                        episodeSelectedIndex = episodeIndex
                                        break
                                    }
                                }
                                break
                            }
                        }
                    }
                    contentRecentSelected.postValue(response.body)
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
        isKids = currentProfile.id == -1
    }
}
