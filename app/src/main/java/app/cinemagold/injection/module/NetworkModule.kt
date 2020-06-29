package app.cinemagold.injection.module

import app.cinemagold.BuildConfig
import app.cinemagold.model.user.User
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton


//Injectable dependencies for remote data access
@Module
class NetworkModule {
    private val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun giveRetrofit(okHttpClient: OkHttpClient, objectMapper: ObjectMapper): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            //Call adapter for error handling with Retrofit2 + Kotlin Coroutines
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun giveOkHttpClient(httpLoggingInterceptor: Interceptor, user : User): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request().newBuilder().addHeader("Cookie", "Authorization="+user.token).build()
                    return chain.proceed(request);
                }
            })
            .build()
    }

    @Provides
    fun giveHttpLoggingInterceptor(): Interceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return httpLoggingInterceptor
    }

    @Provides fun giveObjectMapper() : ObjectMapper {
        return ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))
    }
}