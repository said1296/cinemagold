package app.cinemagold.ui.browse.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers


class HomeViewModel(val contentApi : ContentApi)  : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val contentPremiere : LiveData<MutableList<Content>> = liveData(Dispatchers.IO) {
        when(val data = contentApi.getNew(8)){
            is NetworkResponse.Success -> {
                emit(data.body)
            }
            is NetworkResponse.ServerError -> {
                error.postValue(data.body?.status.toString() + " " + data.body?.message)
            }
            is NetworkResponse.NetworkError -> {
                error.postValue(data.error.toString())
            }
            else -> error.postValue("Unknown error")
        }
    }

    val contentGroupedByGenre : LiveData<MutableList<ContentGroupedByGenre>> = liveData(Dispatchers.IO) {
        when(val data = contentApi.getGroupedByGenre()){
            is NetworkResponse.Success -> {
                emit(data.body)
            }
            is NetworkResponse.ServerError -> {
                error.postValue(data.body?.status.toString() + " " + data.body?.message)
            }
            is NetworkResponse.NetworkError -> {
                error.postValue(data.error.toString())
            }
            else -> error.postValue("Unknown error")
        }
    }
}
