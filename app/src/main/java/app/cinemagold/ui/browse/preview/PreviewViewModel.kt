package app.cinemagold.ui.browse.preview

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.network.NetworkError
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class PreviewViewModel(val contentApi : ContentApi, val context: Context) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val content : LiveEvent<Content> by lazy {
        LiveEvent<Content>()
    }
    var currentContentId = -1
    var currentContentType = ContentType.NONE
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var currentProfile : Profile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)

    //Events
    fun receivedContentIdAndContentType(contentId : Int, contentType: ContentType){
        currentContentId = contentId
        currentContentType = contentType
        requestContent()
    }

    //Request
    private fun requestContent(){
        viewModelScope.launch {
            val response : NetworkResponse<Content, NetworkError> =
                if(currentContentType== ContentType.MOVIE){
                    contentApi.getMovie(currentContentId)
                }else{
                    contentApi.getSerialized(currentContentId, currentProfile.id!!)
                }

            when(response){
                is NetworkResponse.Success -> {
                    content.postValue(response.body)
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
