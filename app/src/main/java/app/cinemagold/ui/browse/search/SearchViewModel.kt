package app.cinemagold.ui.browse.search

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch


class SearchViewModel(private val contentApi : ContentApi, val context : Context)  : ViewModel() {
    val error: LiveEvent<String> by lazy {
            LiveEvent<String>()
    }
    val contentSearch : MutableLiveData<MutableList<Content>> by lazy {
        MutableLiveData<MutableList<Content>>(mutableListOf())
    }
    var partialName : String = ""
    //In milliseconds
    private val cooldownTime : Long = 500

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        requestSearchContentByName()
    }
    private var isKids = false

    init {
        setIsKids()
    }


    //Events
    fun inputSearch(partialNameNew : String){
        if(partialNameNew.isNotEmpty()){
            partialName = partialNameNew
            //Remove previous delayed requests
            handler.removeCallbacks(searchRunnable)
            //Set search for one second from now so the user stops typing
            handler.postDelayed(searchRunnable, cooldownTime)
        }else{
            handler.removeCallbacks(searchRunnable)
        }
    }


    //Requests
    private fun requestSearchContentByName(){
        viewModelScope.launch {
            when(val response = contentApi.searchByName(partialName, 21, isKids)){
                is NetworkResponse.Success -> {
                    contentSearch.postValue(response.body)
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

    //Actions
    //Actions
    private fun setIsKids(){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
        isKids = currentProfile.id == -1
    }
}
