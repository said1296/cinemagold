package app.cinemagold.ui.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.browse.common.fragment.ContentGridFragment
import app.cinemagold.ui.browse.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.browse.home.HomeFragment
import javax.inject.Inject


class AuthenticationActivity : AppCompatActivity() {
    private val fragmentContainer = R.id.nav_host_fragment
    private val fragmentManager = supportFragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        supportFragmentManager.beginTransaction().add(fragmentContainer, HomeFragment(), HomeFragment::class.simpleName).commit()
    }

    override fun onBackPressed() {
        fragmentManager.popBackStack()
    }

    //Fragment transactions
    private fun addOrReplaceFragment(fragment : Fragment, tag : String?){
        if(fragmentManager.findFragmentByTag(tag)==null){
            println("ADDING $tag")
            fragmentManager.beginTransaction().add(R.id.nav_host_fragment, fragment, tag).addToBackStack(tag).commit()
            return
        }

        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount>0){
            val currentFragmentTag = fragmentManager.getBackStackEntryAt(backStackEntryCount-1).name
            if(currentFragmentTag != tag){
                println("REPLACING $tag")
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment, tag).addToBackStack(tag).commit()
            }
        }
    }
}
