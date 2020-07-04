package app.cinemagold.ui.browse.preview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.network.NetworkError
import app.cinemagold.ui.browse.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class PlayerViewModel(private val contentApi : ContentApi) : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}
