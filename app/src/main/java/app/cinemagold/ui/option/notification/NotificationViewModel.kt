package app.cinemagold.ui.option.notification

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.dataaccess.remote.NotificationApi
import app.cinemagold.dataaccess.remote.ProfileApi
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationApi: NotificationApi): ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val notifications : MutableLiveData<List<IdAndName>> by lazy {
        MutableLiveData<List<IdAndName>>(emptyList())
    }

    init {
        requestNotifications()
    }

    private fun requestNotifications(){
        viewModelScope.launch {
            when(val response = notificationApi.getAll()){
                is NetworkResponse.Success -> {
                    notifications.postValue(response.body)
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