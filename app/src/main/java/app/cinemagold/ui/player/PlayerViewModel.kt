package app.cinemagold.ui.player

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.RecentApi
import app.cinemagold.model.content.Recent
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class PlayerViewModel(private val recentApi: RecentApi, val context: Context) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var currentProfile : Profile

    init {
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
    }

    //Events
    fun triggeredUpdateElapsed(contentId : Int, episodeId : Int, elapsed: Int, elapsedPercent : Float){
        val recent = Recent(contentId, elapsed, elapsedPercent, episodeId = episodeId, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    fun triggeredUpdateElapsed(contentId : Int, elapsed: Int, elapsedPercent : Float){
        val recent = Recent(contentId, elapsed, elapsedPercent, profileId = currentProfile.id)
        updateElapsed(recent)
    }

    //Request
    fun updateElapsed(recent : Recent){
        viewModelScope.launch {
            when(val response = recentApi.saveRecent(recent)){
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
