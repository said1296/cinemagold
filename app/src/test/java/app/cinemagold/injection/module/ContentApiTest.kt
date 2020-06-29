package app.cinemagold.injection.module

import app.cinemagold.dataaccess.remote.ContentApi
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ContentApiTest {
    @Rule
    @JvmField
    var mockitoRule: MockitoRule = MockitoJUnit.rule()


    var networkModule : NetworkModule = NetworkModule()
    lateinit var contentApi : ContentApi

/*    @Before
    fun setUp() {
        val retroFit = networkModule.giveRetrofit(networkModule.giveOkHttpClient(networkModule.giveHttpLoggingInterceptor(),
            User("Said")
        ))
        contentApi = retroFit.create(ContentApi::class.java)
    }*/

    @After
    fun tearDown() {
        Thread.sleep(10000);
    }

    @Test
    suspend fun getContentGroupedByGenre() {
        contentApi.getGroupedByGenre()
    }
}