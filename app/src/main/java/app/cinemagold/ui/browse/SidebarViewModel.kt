package app.cinemagold.ui.browse

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.dataaccess.remote.NotificationApi
import app.cinemagold.dataaccess.remote.ProfileApi
import app.cinemagold.model.generic.LongIdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class SidebarViewModel(val authenticationApi: AuthenticationApi, val profileApi: ProfileApi, private val notificationApi: NotificationApi, context : Context) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val isOpenProfiles : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    val isOpenDevices : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    val notificationsCount : MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }
    lateinit var profiles : List<Profile>
    lateinit var devices : List<LongIdAndName>
    var layoutInflater : LayoutInflater = LayoutInflater.from(context)

    init {
        requestNotifications()
    }

    //Events
    fun deauthAllDevices(){
        requestDeauthAll()
    }

    fun clickedProfiles(){
        if(isOpenProfiles.value!=null && isOpenProfiles.value!!){
            isOpenProfiles.value=false
        }else{
            isOpenDevices.value = false
            requestProfiles()
        }
    }

    fun clickedDevices(){
        if(isOpenDevices.value!=null && isOpenDevices.value!!){
            isOpenDevices.value=false
        }else{
            isOpenProfiles.value = false
            requestDevices()
        }
    }

    fun clickedDeauth(device : LongIdAndName){
        requestDeauth(device.id)
    }

    //Requests
    private fun requestProfiles(){
        viewModelScope.launch {
            when(val response = profileApi.getProfiles()){
                is NetworkResponse.Success -> {
                    profiles = response.body
                    isOpenProfiles.postValue(true)
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

    private fun requestDevices(){
        viewModelScope.launch {
            when(val response = authenticationApi.getDevices()){
                is NetworkResponse.Success -> {
                    devices = response.body
                    isOpenDevices.postValue(true)
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

    private fun requestDeauth(id : Long){
        viewModelScope.launch {
            when(val response = authenticationApi.deauthDevice(id)){
                is NetworkResponse.Success -> {
                    isOpenDevices.postValue(false)
                    requestDevices()
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

    private fun requestDeauthAll(){
        viewModelScope.launch {
            when(val response = authenticationApi.deauthAllDevices()){
                is NetworkResponse.Success -> {
                    isOpenDevices.postValue(false)
                    devices = emptyList()
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

    private fun requestNotifications(){
        viewModelScope.launch {
            when(val response = notificationApi.getAll()){
                is NetworkResponse.Success -> {
                    notificationsCount.postValue(response.body.size)
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