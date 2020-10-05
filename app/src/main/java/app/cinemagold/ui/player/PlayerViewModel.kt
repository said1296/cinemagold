package app.cinemagold.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
    private var seasonIndex : Int = 0
    private var episodeIndex : Int = 0
    var elapsed = -1L
    private var currentProfile: Profile

    init {
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
    }

    //Events
    fun triggeredUpdateElapsed(contentId: Int, episodeId: Int, elapsed: Int, elapsedPercent: Float) {
        val recent = Recent(contentId, elapsed, elapsedPercent, episodeId = episodeId, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    fun receivedExtras(extras : Bundle?){
        if(extras != null){
            content = Gson().fromJson(extras.get("content") as String, Content::class.java)
            if(content.mediaType.id != ContentType.MOVIE.value){
                episodeIndex = extras.getInt("episodeIndex")
                seasonIndex = extras.getInt("seasonIndex")
            }
            //Get elapsed time if a Recently Played content was clicked
            if(extras.getInt("elapsed") != -1){
                elapsed = extras.getInt("elapsed").toLong()
            }
        }
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

    fun videoPaused(){
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
}
