package app.cinemagold.ui.option.profile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.R
import app.cinemagold.dataaccess.remote.ProfileApi
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileApi: ProfileApi, private val context: Context)  : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val profiles: MutableLiveData<MutableList<Profile>> by lazy {
        MutableLiveData<MutableList<Profile>>()
    }
    val isSuccessful: LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    var isEdit = false

    //Events
    fun startedFragment(){
        requestProfiles()
    }

    fun selectedProfile(profilePosition: Int){
        val selectedProfile = profiles.value!![profilePosition]
        savePrefsProfile(selectedProfile)
    }

    fun selectedKids(){
        savePrefsProfile(
            Profile(-1,
                IdAndName(1, "https://www.ibbymexico.org.mx/dos/wp-content/uploads/revslider/splash-creative-light-01-animated/Slider-CL01-Background-1024x709.png"),
                    context.resources.getString(R.string.KIDS)
                )
        )
    }

    fun receivedIsEdit(isEdit : Boolean){
        this.isEdit = isEdit
    }

    //Requests
    private fun requestProfiles(){
        viewModelScope.launch {
            when(val response = profileApi.getProfiles()){
                is NetworkResponse.Success -> {
                    profiles.postValue(response.body)
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
    private fun savePrefsProfile(profile : Profile){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
        preferences.putString(BuildConfig.PREFS_PROFILE, Gson().toJson(profile)).apply()
        if(preferences.commit()){
            isSuccessful.value = true
        }else{
            error.value = "No se pudo seleccionar"
        }
    }
}
