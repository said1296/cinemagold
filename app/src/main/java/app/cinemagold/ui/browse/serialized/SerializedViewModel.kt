package app.cinemagold.ui.browse.serialized

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.dataaccess.remote.ContentApi
import app.cinemagold.dataaccess.remote.ContentTypeApi
import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.common.LiveEvent
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.launch

class SerializedViewModel(
    private val contentApi: ContentApi,
    private val genreApi: GenreApi,
    private val contentTypeApi: ContentTypeApi,
    val context: Context
) : ViewModel() {
    val error: LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val contentGroupedByGenre: MutableLiveData<MutableList<ContentGroupedByGenre>> by lazy {
        MutableLiveData<MutableList<ContentGroupedByGenre>>()
    }
    val years: MutableLiveData<MutableList<IdAndName>> by lazy {
        MutableLiveData<MutableList<IdAndName>>()
    }
    val genres: MutableLiveData<MutableList<IdAndName>> by lazy {
        MutableLiveData<MutableList<IdAndName>>()
    }
    val contentGenre: LiveEvent<MutableList<Content>> by lazy {
        LiveEvent<MutableList<Content>>()
    }
    val contentTypes: MutableLiveData<MutableList<IdAndName>> by lazy {
        MutableLiveData<MutableList<IdAndName>>()
    }
    var currentGenre = IdAndName()
    var currentYear = IdAndName()
    var contentTypeSpinnerItems = mutableListOf<String>()
    var currentContentType = IdAndName()
    var isKids = false

    fun initialize() {
        setIsKids()
        requestContentTypesAndContentTypeSpinnerItems()
        requestGenres()
        requestYears()
        handleRequestContent()
    }

    //Events
    fun selectedContentType(contentTypeIndex: Int) {
        if (currentContentType != contentTypes.value!![contentTypeIndex]) {
            currentContentType = contentTypes.value!![contentTypeIndex]
            requestGenres()
            requestYears()
            handleRequestContent()
        }
    }

    fun selectedGenre(genre: IdAndName) {
        if (currentGenre != genre) {
            currentGenre = genre
            currentYear = IdAndName(-1, "Todos")
            requestYears()
            handleRequestContent()
        }
    }

    fun selectedYear(year: IdAndName) {
        if (currentYear != year) {
            currentYear = year
            handleRequestContent()
        }
    }

    fun handleRequestContent() {
        // id of -1 means "All" in filters
        if (currentGenre.id == -1 && currentYear.id == -1) {
            requestContentGroupedByGenre()
        } else if (currentYear.id == -1 && currentGenre.id != -1) {
            requestContentByGenreAndContentType()
        } else if (currentYear.id != -1) {
            requestContentByYear()
        }
    }

    fun stoppedFragment() {
        //Reinitialize genre value
        currentGenre = IdAndName()
    }


    //Requests
    private fun requestContentByYear() {
        viewModelScope.launch {
            val response = if (currentContentType.id != ContentType.NONE.value)
                contentApi.getByContentTypeIdAndYear(
                    currentYear.id,
                    currentContentType.id,
                    if (currentGenre.id == -1) null else currentGenre.id,
                    60
                )
            else contentApi.getSerializedByYear(
                currentYear.id,
                if (currentGenre.id == -1) null else currentGenre.id,
                60
            )
            when (response) {
                is NetworkResponse.Success -> {
                    contentGenre.postValue(response.body)
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

    private fun requestContentGroupedByGenre() {
        viewModelScope.launch {
            val response =
                if (currentContentType.id == -1) {
                    contentApi.getSerializedGroupedByGenre(isKids)
                } else {
                    contentApi.getByContentTypeIdAndGroupedByGenre(currentContentType.id, isKids, 1)
                }
            when (response) {
                is NetworkResponse.Success -> {
                    contentGroupedByGenre.postValue(response.body)
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

    private fun requestContentByGenreAndContentType() {
        viewModelScope.launch {
            val response =
                if (currentContentType.id == -1) {
                    contentApi.getSerializedByGenre(currentGenre.id, 21, isKids)
                } else {
                    contentApi.getByGenreIdAndOptionalContentTypeId(currentGenre.id, currentContentType.id, 60, isKids)
                }
            when (response) {
                is NetworkResponse.Success -> {
                    contentGenre.postValue(response.body)
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

    private fun requestGenres() {
        viewModelScope.launch {
            val response =
                if (currentContentType.id == -1) {
                    genreApi.getSerialized()
                } else {
                    genreApi.getByContentTypeId(currentContentType.id)
                }
            when (response) {
                is NetworkResponse.Success -> {
                    response.body.add(0, IdAndName(-1, "Todos"))
                    genres.postValue(response.body)
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

    private fun requestContentTypesAndContentTypeSpinnerItems() {
        viewModelScope.launch {
            when (val data = contentTypeApi.getAll()) {
                is NetworkResponse.Success -> {
                    data.body.removeAll { contentType -> contentType.id == ContentType.MOVIE.value }
                    data.body.add(0, IdAndName(-1, "Todos"))
                    //Set content type spinner items
                    for (contentType in data.body) {
                        contentTypeSpinnerItems.add(contentType.name)
                    }
                    contentTypes.postValue(data.body)
                }
                is NetworkResponse.ServerError -> {
                    error.postValue(data.body?.status.toString() + " " + data.body?.message)
                }
                is NetworkResponse.NetworkError -> {
                    error.postValue(data.error.toString())
                    println(data.error.toString())
                }
                else -> error.postValue("Unknown error")
            }
        }
    }

    private fun requestYears() {
        viewModelScope.launch {
            val response = if (currentContentType.id != ContentType.NONE.value)
                contentApi.getYearsByContentTypeIdAndOptionalGenreId(
                    currentContentType.id,
                    if (currentGenre.id == -1) null else currentGenre.id
                )
                else contentApi.getSerializedYearsByOptionalGenreId(if (currentGenre.id == -1) null else currentGenre.id)
            when (response) {
                is NetworkResponse.Success -> {
                    // Put years in IdAndName objects so it can be used by FilterCarrouselRVA
                    val yearsUpdated: MutableList<IdAndName> = mutableListOf()
                    yearsUpdated.add(0, IdAndName(-1, "Todos"))
                    for (year in response.body) {
                        yearsUpdated.add(IdAndName(year, year.toString()))
                    }
                    years.postValue(yearsUpdated)
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

    //Actions
    private fun setIsKids() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)
        isKids = currentProfile.id == -1
    }
}
