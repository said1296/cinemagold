package app.cinemagold.ui.authentication.register

import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.R
import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.dataaccess.remote.CountryApi
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.RegisterForm
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class RegisterViewModel(private val authenticationApi: AuthenticationApi, private val countryApi: CountryApi) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val isSuccessful : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    lateinit var countries : MutableList<IdAndName>
    var selectedCountry = IdAndName()
    val countrySpinnerItems : MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>()
    }

    init {
        requestCountries()
    }

    //Events
    fun selectedCountry(country: String){
        val selectedCountryIndex = countrySpinnerItems.value!!.indexOf(country)
        selectedCountry = countries[selectedCountryIndex]
    }
    fun submit(formView : View){
        val registerForm = RegisterForm()
        registerForm.name = formView.findViewById<AppCompatEditText>(R.id.register_name).text.toString()
        registerForm.lastname = formView.findViewById<AppCompatEditText>(R.id.register_lastname).text.toString()
        registerForm.phone = formView.findViewById<AppCompatEditText>(R.id.register_phone_number).text.toString()
        registerForm.mail = formView.findViewById<AppCompatEditText>(R.id.register_email).text.toString()
        registerForm.password = formView.findViewById<AppCompatEditText>(R.id.register_password_edit_text).text.toString()
        registerForm.countryId = selectedCountry.id

        if(registerForm.name.isNullOrBlank()){
            error.value = "Proporciona un nombre"
            return
        }
        else if (registerForm.lastname.isNullOrBlank()){
            error.value = "Proporciona tus apellidos"
            return
        }
        else if(registerForm.phone.isNullOrBlank()){
            error.value = "Proporciona tu teléfono"
            return
        }
        else if(registerForm.countryId == -1){
            error.value = "Selecciona tu país"
            return
        }
        else if(registerForm.mail.isNullOrEmpty()){
            error.value = "Proporciona un correo"
            return
        }
        else if(registerForm.password.isNullOrEmpty()){
            error.value = "Proporciona una contraseña"
            return
        }

        requestRegistration(registerForm)
    }

    //Requests
    private fun requestCountries(){
        viewModelScope.launch {
            when(val response = countryApi.getAll()){
                is NetworkResponse.Success -> {
                    countries = response.body
                    countries.add(0, IdAndName(-1, "País"))
                    countrySpinnerItems.postValue(
                        countries.stream().map { item ->
                            item.name
                        }.collect(Collectors.toList())
                    )
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

    private fun requestRegistration(registerForm : RegisterForm){
        viewModelScope.launch {
            when(val response = authenticationApi.register(registerForm)){
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
