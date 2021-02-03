package app.cinemagold.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.PlayerApi
import app.cinemagold.dataaccess.remote.RecentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.Recent
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class PlayerViewModel(private val recentApi: RecentApi, private val playerApi: PlayerApi, val context: Context) :
    ViewModel() {
    val error: LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val authorizationError: LiveEvent<IdAndName> by lazy {
        LiveEvent<IdAndName>()
    }
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    lateinit var content : Content
    var seasonIndex : Int = -1
    var episodeIndex : Int = 0
    var elapsed = -1L
    val title: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private var currentProfile: Profile

    init {
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
    }

    //Events

    fun receivedExtras(extras : Bundle?){
        if(extras != null){
            content = Gson().fromJson(extras.get(PlayerActivity.EXTRAS_CONTENT) as String, Content::class.java)
            if(content.mediaType.id != ContentType.MOVIE.value) {
                episodeIndex = extras.getInt(PlayerActivity.EXTRAS_EPISODE_INDEX)
                seasonIndex = extras.getInt(PlayerActivity.EXTRAS_SEASON_INDEX)
            }

            setTitle()

            //Get elapsed time if a Recently Played content was clicked
            if(extras.getInt(PlayerActivity.EXTRAS_ELAPSED) != -1){
                elapsed = extras.getInt(PlayerActivity.EXTRAS_ELAPSED).toLong()
            }
        }
    }

    fun changedEpisode(episodeIndexNew: Int){
        episodeIndex = episodeIndexNew
        setTitle()
    }

    fun videoEnded(){
        if(content.mediaType.id != ContentType.MOVIE.value){
            val currentSeasonLength = content.seasons[seasonIndex].episodes.size
            if(episodeIndex < (currentSeasonLength-1)) episodeIndex++
            else{
                val contentLength = content.seasons.size
                if(seasonIndex < contentLength-1) seasonIndex++
                else seasonIndex = 0

                episodeIndex=0
            }
            setTitle()
        }
    }

    fun triggeredUpdateElapsed(elapsed: Int, elapsedPercent: Float) {
        val recent =
            if(content.mediaType.id == ContentType.MOVIE.value)
                Recent(content.id, elapsed, elapsedPercent, profileId = currentProfile.id)
            else
                Recent(content.id, elapsed, elapsedPercent, episodeId = content.seasons[seasonIndex].episodes[episodeIndex].id, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    fun videoPlaying(remaining: Long) {
        requestUserState(remaining, content.mediaType.id)
    }

    fun videoIdle(){
        requestDeleteAuthorization()
    }

    //Request
    private fun updateElapsed(recent: Recent) {
        viewModelScope.launch {
            when (val response = recentApi.saveRecent(recent)) {
                is NetworkResponse.Success -> {
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

    private fun requestUserState(remaining: Long, contentTypeId: Int) {
        viewModelScope.launch {
            when (val response = playerApi.authorize(remaining, contentTypeId)) {
                is NetworkResponse.Success -> {
                    //If id==0 then it was authorized
                    if(response.body.id != 0){
                        authorizationError.postValue(response.body)
                    }
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

    private fun requestDeleteAuthorization(){
        viewModelScope.launch {
            when (val response = playerApi.deleteAuthorization()) {
                is NetworkResponse.Success -> {
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


    // Actions

    fun setTitle(){
        title.value = if(content.mediaType.id == ContentType.MOVIE.value) {
            content.name
        } else {
            "T${content.seasons[seasonIndex].number} · Cap.${content.seasons[seasonIndex].episodes[episodeIndex].number}: ${content.seasons[seasonIndex].episodes[episodeIndex].name}"
        }
    }
}
