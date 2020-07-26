package app.cinemagold.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.browse.common.fragment.ContentGridFragment
import app.cinemagold.ui.browse.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.browse.home.HomeFragment
import app.cinemagold.ui.browse.movie.MovieFragment
import app.cinemagold.ui.browse.preview.PreviewFragment
import app.cinemagold.ui.browse.search.SearchFragment
import app.cinemagold.ui.browse.serialized.SerializedFragment
import app.cinemagold.ui.player.PlayerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import javax.inject.Inject


class BrowseActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_browse)
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
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    addOrReplaceFragment(SerializedFragment(), SerializedFragment::class.simpleName)
                    //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_home -> {
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName)
                    //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_movie -> {
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    addOrReplaceFragment(MovieFragment(), MovieFragment::class.simpleName)
                    //Force ContentGroupedByGenreFragment to run onCreateView with MovieFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_search -> {
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    addOrReplaceFragment(SearchFragment(), SearchFragment::class.simpleName)
                    //Force ContentGroupedByGenreFragment to run onCreateView with MovieFragment now as main view
                    refreshFragmentByTag(contentGridFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
        }
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

    fun navigateToPreview(contentId : Int, contentTypeId: Int){
        //Set contentId and ContentType for Preview
        fragmentManager.setFragmentResult("preview", bundleOf("contentId" to contentId, "contentType" to ContentType.from(contentTypeId)))
        addOrReplaceFragment(PreviewFragment(), PreviewFragment::class.simpleName)
    }

    fun changeContentGrid(contents : List<Content>){
        val fragment = fragmentManager.findFragmentByTag(ContentGridFragment::class.simpleName)
        if(fragment == null || !fragment.isVisible){
            fragmentManager.beginTransaction()
                .replace(contentContainer, contentGridFragment, ContentGridFragment::class.simpleName).commit()
        }
        contentGridFragment.updateContents(contents)
    }

    fun changeContentGroupedByGenre(contents : List<ContentGroupedByGenre>){
        val fragment = fragmentManager.findFragmentByTag(ContentGroupedByGenreFragment::class.simpleName)
        if(fragment == null || !fragment.isVisible || !fragment.isDetached){
            fragmentManager.beginTransaction()
                .replace(contentContainer, contentGroupedByGenreFragment, ContentGroupedByGenreFragment::class.simpleName).commit()
        }
        contentGroupedByGenreFragment.updateContents(contents)
    }

    private fun refreshFragmentByTag(tag : String?){
        println("REFRESHING REQUEST")
        val fragment = fragmentManager.findFragmentByTag(tag)
        if(fragment!=null){
            println("REFRESHING FRAGMENT")
            fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit()
        }
    }

    private fun detachFragmentByTag(tag : String?){
        val fragment = fragmentManager.findFragmentByTag(tag)
        if(fragment!=null){
            fragmentManager.beginTransaction().detach(fragment).commit()
        }
    }

    // Activity navigation

    fun navigateToPlayer(content : Content){
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("content", Gson().toJson(content))
        startActivity(intent)
    }
}
