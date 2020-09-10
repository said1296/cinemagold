package app.cinemagold.ui.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.ui.common.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val authenticationApi: AuthenticationApi) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val isSuccessful : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    //Events
    fun submit(email : String, password : String){
        viewModelScope.launch {
            when(val response = authenticationApi.authenticate(email, password)){
                is NetworkResponse.Success -> {
                    if(!response.body.status){
                        error.postValue(response.body.error?.get(0))
                    }else{
                        isSuccessful.postValue(true)
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
}
