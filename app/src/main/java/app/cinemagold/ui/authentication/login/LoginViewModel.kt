package app.cinemagold.ui.authentication.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.AuthenticationApi
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class LoginViewModel(val authenticationApi: AuthenticationApi) : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //Events
    fun submit(email : String, password : String){
        viewModelScope.launch {
            when(val response = authenticationApi.authenticate(email, password)){
                is NetworkResponse.Success -> {
                    if(response.body.status){
                        println("NICE")
                        println(response.body)
                    }else{
                        println("NOT NICE >:|")
                        println(response.body)
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
