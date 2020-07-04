package app.cinemagold.ui.browse.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch


class SearchViewModel(private val contentApi : ContentApi)  : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val contentSearch : MutableLiveData<MutableList<Content>> by lazy {
        MutableLiveData<MutableList<Content>>(mutableListOf())
    }
    var partialName : String = ""
    //In milliseconds
    private val cooldownTime : Long = 1000

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        requestSearchContentByName()
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
            when(val response = contentApi.searchByName(partialName, 21)){
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
}
