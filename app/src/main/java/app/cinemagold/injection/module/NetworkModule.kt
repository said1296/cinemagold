package app.cinemagold.injection.module

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.AuthenticationActivity
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
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.*


//Injectable dependencies for remote data access
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun giveRetrofit(okHttpClient: OkHttpClient, objectMapper: ObjectMapper): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            //Call adapter for error handling with Retrofit2 + Kotlin Coroutines
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun giveOkHttpClient(httpLoggingInterceptor: Interceptor,
                         @Named("handleCookiesInterceptor") handleCookiesInterceptor: Interceptor,
                         @Named("addCookiesInterceptor") addCookiesInterceptor: Interceptor,
                         @Named("forbiddenInterceptor") forbiddenInterceptor: Interceptor): OkHttpClient {
        val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?>?{
                    return arrayOfNulls(0)
                }
            }
        )

        // Install the all-trusting trust manager

        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.getSocketFactory()

        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(object: Interceptor{
                override fun intercept(chain: Interceptor.Chain): Response {
                    val originalRequest = chain.request()
                    val requestWithUserAgent = originalRequest.newBuilder()
                        .header("User-Agent", Build.MODEL)
                        .build()
                    return chain.proceed(requestWithUserAgent)                }
            })
            .addInterceptor(handleCookiesInterceptor)
            .addInterceptor(addCookiesInterceptor)
            .addInterceptor(forbiddenInterceptor)
            //TODO: Delete these two for production
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .build()
    }

    @Provides
    fun giveHttpLoggingInterceptor(): Interceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return httpLoggingInterceptor
    }

    @Provides
    fun giveObjectMapper() : ObjectMapper {
        return ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))
    }

    @Provides
    @Named("handleCookiesInterceptor")
    fun giveHandleCookiesInterceptor(context: Context) : Interceptor {
        return object: Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalResponse = chain.proceed(chain.request())
                if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
                    val cookies = PreferenceManager.getDefaultSharedPreferences(context)
                        .getStringSet(BuildConfig.PREFS_COOKIES, HashSet()) as HashSet<String>?
                    for (header in originalResponse.headers("Set-Cookie")) {
                        cookies!!.add(header)
                    }
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
                    preferences.putStringSet(BuildConfig.PREFS_COOKIES, cookies).apply()
                    preferences.commit()
                }
                return originalResponse
            }
        }
    }

    @Provides
    @Named("addCookiesInterceptor")
    fun giveAddCookiesInterceptor(context: Context) : Interceptor {
        return object: Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var cookieString = ""
                val builder: Request.Builder = chain.request().newBuilder()
                val preferencesCookies =
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .getStringSet(BuildConfig.PREFS_COOKIES, HashSet()) as HashSet<String>?
                if (preferencesCookies != null) {
                    for (cookie in preferencesCookies) {
                        val parser = cookie.split(";");
                        cookieString = cookieString + parser[0] + "; ";
                    }
                    builder.addHeader("Cookie", cookieString);
                }
                return chain.proceed(builder.build())
            }
        }
    }

    @Provides
    @Named("forbiddenInterceptor")
    fun hiveHandleForbiddenInterceptor(context : Context) : Interceptor {
        return object: Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val response = chain.proceed(request)
                if(response.code==403){
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    (context as ApplicationContextInjector).startActivity(intent)
                }
                return response
            }
        }
    }
}