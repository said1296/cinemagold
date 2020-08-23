package app.cinemagold.ui.player

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.PlayerApi
import app.cinemagold.dataaccess.remote.RecentApi
import app.cinemagold.model.content.Recent
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.dataholder.LiveEvent
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
    private var currentProfile: Profile

    init {
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
    }

    //Events
    fun triggeredUpdateElapsed(contentId: Int, episodeId: Int, elapsed: Int, elapsedPercent: Float) {
        val recent = Recent(contentId, elapsed, elapsedPercent, episodeId = episodeId, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    fun triggeredUpdateElapsed(contentId: Int, elapsed: Int, elapsedPercent: Float) {
        val recent = Recent(contentId, elapsed, elapsedPercent, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    fun videoPlaying(remaining: Long, contentTypeId: Int) {
        requestUserState(remaining, contentTypeId)
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
