package app.cinemagold.ui.browse.preview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content

class PlayerViewModel(private val contentApi : ContentApi) : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    lateinit var content : Content

}
