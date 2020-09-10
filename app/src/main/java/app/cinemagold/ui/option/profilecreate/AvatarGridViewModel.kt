package app.cinemagold.ui.option.profilecreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import app.cinemagold.dataaccess.remote.ProfileApi
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.ui.common.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers

class AvatarGridViewModel(val profileApi: ProfileApi) : ViewModel() {
    val error : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val avatars : LiveData<List<IdAndName>> = liveData(Dispatchers.IO) {
        when(val data = profileApi.getAvatars()){
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
