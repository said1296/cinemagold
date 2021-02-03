package app.cinemagold.ui.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.R
import app.cinemagold.dataaccess.remote.AuthenticationApi
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.signin.SignInFragment
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.option.OptionActivity
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AuthenticationActivity : AppCompatActivity() {
    private val fragmentContainer = R.id.fragment_container_authentication
    private val fragmentManager = supportFragmentManager
    @Inject
    lateinit var authenticationApi: AuthenticationApi
    lateinit var preferences : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?){
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        preferences =  PreferenceManager.getDefaultSharedPreferences(this)
        runBlocking {
            withContext(Dispatchers.IO) {
                checkAuthenticate()
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        supportFragmentManager.beginTransaction().add(fragmentContainer, SignInFragment(), SignInFragment::class.simpleName).commit()
    }

    override fun onBackPressed(){
        fragmentManager.popBackStack()
    }

    private suspend fun checkAuthenticate(){
        when(val response = authenticationApi.isAuthenticated()){
            is NetworkResponse.Success -> {
                if(response.body.status){
                    navigateToBrowse()
                }else{
                    //DELETE COOKIES FOR FRESH LOG IN
                    preferences.edit().remove(BuildConfig.PREFS_COOKIES).apply()
                }
            }
            is NetworkResponse.ServerError -> {
                runOnUiThread {
                    toast(response.body?.status.toString() + " " + response.body?.message)
                }
                navigateToBrowse()
            }
            is NetworkResponse.NetworkError -> {
                runOnUiThread {
                    toast(response.error.toString())
                }
                navigateToBrowse()
            }
            else -> {
                runOnUiThread {
                    toast("Unknown error")
                }
                navigateToBrowse()
            }
        }
    }

    private fun toast(message : String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //Fragment transactions
    fun addOrReplaceFragment(fragment : Fragment, tag : String?){
        if(fragmentManager.findFragmentByTag(tag)==null){
            fragmentManager.beginTransaction().add(fragmentContainer, fragment, tag).addToBackStack(tag).commit()
            return
        }

        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount>0){
            val currentFragmentTag = fragmentManager.getBackStackEntryAt(backStackEntryCount-1).name
            if(currentFragmentTag != tag){
                fragmentManager.beginTransaction().replace(fragmentContainer, fragment, tag).addToBackStack(tag).commit()
            }
        }
    }

    fun navigateToBrowse(){
        val intent = Intent(applicationContext, BrowseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }



    fun navigateToOption(fragmentToLoad : String, isEdit: Boolean? = null){
        val intent = Intent(this, OptionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("FRAGMENT", fragmentToLoad)
        //Send information about where the intent came from
        intent.putExtra("ORIGIN", this::class.simpleName)
        isEdit?.also { intent.putExtra("IS_EDIT", isEdit) }
        startActivity(intent)
    }
}
