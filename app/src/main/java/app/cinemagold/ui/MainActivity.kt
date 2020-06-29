package app.cinemagold.ui

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.common.fragment.ContentGridFragment
import app.cinemagold.ui.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.home.HomeFragment
import app.cinemagold.ui.preview.PreviewFragment
import app.cinemagold.ui.movie.MovieFragment
import app.cinemagold.ui.serialized.SerializedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var contentGridFragment: ContentGridFragment
    @Inject
    lateinit var contentGroupedByGenreFragment : ContentGroupedByGenreFragment
    private val contentContainer = R.id.content_container
    private val fragmentContainer = R.id.nav_host_fragment
    private val fragmentManager = supportFragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().add(fragmentContainer, HomeFragment(), HomeFragment::class.simpleName).commit()

        val drawer = findViewById<DrawerLayout>(R.id.sidebar_drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        drawer.closeDrawer(Gravity.START)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navbar)
        bottomNavigationView.setOnNavigationItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.navbar_serialized -> {
                    addOrReplaceFragment(SerializedFragment(), SerializedFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_home -> {
                    addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_movie -> {
                    addOrReplaceFragment(MovieFragment(), MovieFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack()
    }

    fun addOrReplaceFragment(fragment : Fragment, tag : String?){
        if(supportFragmentManager.findFragmentByTag(tag)==null){
            println("ADDING $tag")
            supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment, fragment, tag).addToBackStack(tag).commit()
            return
        }

        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount>0){
            val currentFragmentTag = supportFragmentManager.getBackStackEntryAt(backStackEntryCount-1).name
            if(currentFragmentTag != tag){
                println("REPLACING $tag")
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment, tag).addToBackStack(tag).commit()
            }
        }
    }

    fun navigateToPreview(contentId : Int, contentTypeId: Int){
        //Set contentId and ContentType for Preview
        supportFragmentManager.setFragmentResult("preview", bundleOf("contentId" to contentId, "contentType" to ContentType.from(contentTypeId)))
        addOrReplaceFragment(PreviewFragment(), PreviewFragment::class.simpleName)
    }

    fun changeContentGrid(contents : List<Content>){
        val fragment = fragmentManager.findFragmentByTag(ContentGridFragment::class.simpleName)
        if(fragment == null || !fragment.isVisible){
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.replace(contentContainer, contentGridFragment, ContentGridFragment::class.simpleName).commit()
        }
        contentGridFragment.updateContents(contents)
    }

    fun changeContentGroupedByGenre(contents : List<ContentGroupedByGenre>){
        val fragment = fragmentManager.findFragmentByTag(ContentGroupedByGenreFragment::class.simpleName)
        if(fragment == null || !fragment.isVisible){
            println("REPLACING")
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.replace(contentContainer, contentGroupedByGenreFragment, ContentGroupedByGenreFragment::class.simpleName).commit()
        }
        contentGroupedByGenreFragment.updateContents(contents)
    }

    fun refreshFragmentByTag(tag : String?){
        println("REFRESHING FRAGMENT")
        val fragment = fragmentManager.findFragmentByTag(tag)
        if(fragment!=null){
            fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit()
        }
    }

    private fun detachFragmentByTag(tag : String?){
        println("DETACHING")
        val fragment = fragmentManager.findFragmentByTag(tag)
        if(fragment!=null){
            println("DETACHING $tag")
            fragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
}
