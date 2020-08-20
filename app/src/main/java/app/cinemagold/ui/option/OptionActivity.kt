package app.cinemagold.ui.option

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.cinemagold.R
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.option.help.HelpFragment
import app.cinemagold.ui.option.help.PaymentFragment
import app.cinemagold.ui.option.profile.ProfileFragment


class OptionActivity : AppCompatActivity() {
    private val fragmentContainer = R.id.fragment_container_option
    private val fragmentManager = supportFragmentManager
    lateinit var fragment : Fragment


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)
        fragment = when(intent.getStringExtra("FRAGMENT")){
            HelpFragment::class.simpleName -> HelpFragment()
            PaymentFragment::class.simpleName -> PaymentFragment()
            else -> {
                ProfileFragment()
            }
        }
        supportFragmentManager.beginTransaction().add(fragmentContainer, fragment, fragment::class.simpleName).commit()
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
        val intent = Intent(this, BrowseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
