package app.cinemagold.ui.preview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.network.NetworkError
import app.cinemagold.ui.common.dataholder.LiveEvent
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class PreviewViewModel(val contentApi : ContentApi) : ViewModel() {
    val error : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val content : LiveEvent<Content> by lazy {
        LiveEvent<Content>()
    }
    var currentContentId = -1
    var currentContentType = ContentType.NONE

    //Events
    fun receivedContentIdAndContentType(contentId : Int, contentType: ContentType){
        currentContentId = contentId
        currentContentType = contentType
        requestContent()
    }

    //Request
    private fun requestContent(){
        viewModelScope.launch {

            val response : NetworkResponse<Content, NetworkError> =
                if(currentContentType==ContentType.MOVIE){
                    contentApi.getMovie(currentContentId)
                }else{
                    contentApi.getSerialized(currentContentId)
                }

            when(response){
                is NetworkResponse.Success -> {
                    content.postValue(response.body)
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
