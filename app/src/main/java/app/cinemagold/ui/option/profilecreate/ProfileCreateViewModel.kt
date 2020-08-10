package app.cinemagold.ui.option.profilecreate

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ProfileApi
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.android.synthetic.main.fragment_profile_create.view.*
import kotlinx.coroutines.launch

class ProfileCreateViewModel(private val profileApi: ProfileApi, private val context: Context)  : ViewModel() {
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val isSuccessful : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    var isEdit = false
    var profile : Profile = Profile(null, IdAndName(-1, ""), "")

    //Events
    fun selectedAvatar(avatar: IdAndName){
        profile.avatar = avatar
    }

    fun submit(formView : View){
        profile.name = formView.profile_create_name.text.toString()
        if(profile.avatar.id == -1){
            error.value = "Selecciona un avatar"
        }
        else if(profile.name.isBlank()){
            error.value = "Selecciona un nombre"
        }else{
            requestCreateOrUpdateProfile()
        }
    }

    fun receivedIsEdit(isEdit : Boolean, trueCallback : () -> Unit){
        this.isEdit = isEdit
        if(isEdit){
            profile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
            trueCallback()
        }
    }

    //Requests
    private fun requestCreateOrUpdateProfile(){
        viewModelScope.launch {
            when(val response = profileApi.createProfile(profile)){
                is NetworkResponse.Success -> {
                    preferences.edit().putString(BuildConfig.PREFS_PROFILE, Gson().toJson(response.body)).apply()
                    isSuccessful.postValue(true)
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