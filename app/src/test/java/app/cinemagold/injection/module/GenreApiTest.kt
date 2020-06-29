package app.cinemagold.injection.module

import app.cinemagold.dataaccess.remote.GenreApi
import app.cinemagold.model.user.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule


class GenreApiTest {

    @Rule @JvmField
    var mockitoRule: MockitoRule = MockitoJUnit.rule()


    var networkModule : NetworkModule = NetworkModule()
    lateinit var genreApi : GenreApi

    @Before
    fun setUp() {
        val retroFit = networkModule.giveRetrofit(
            networkModule.giveOkHttpClient(
                networkModule.giveHttpLoggingInterceptor(),
                User("Said")
            ),
            ObjectMapper()
            )
        genreApi = retroFit.create(GenreApi::class.java)
    }

    @After
    fun tearDown() {
    }
}