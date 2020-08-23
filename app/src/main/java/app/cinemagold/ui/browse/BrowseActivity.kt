package app.cinemagold.ui.browse

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.user.Profile
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.browse.common.fragment.ContentGridFragment
import app.cinemagold.ui.browse.common.fragment.ContentGroupedByGenreFragment
import app.cinemagold.ui.browse.home.HomeFragment
import app.cinemagold.ui.browse.movie.MovieFragment
import app.cinemagold.ui.browse.preview.PreviewFragment
import app.cinemagold.ui.browse.search.SearchFragment
import app.cinemagold.ui.browse.serialized.SerializedFragment
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.help.HelpFragment
import app.cinemagold.ui.option.help.PaymentFragment
import app.cinemagold.ui.option.notification.NotificationFragment
import app.cinemagold.ui.option.profile.ProfileFragment
import app.cinemagold.ui.player.PlayerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.common_sidebar.view.*
import kotlinx.android.synthetic.main.sidebar_device.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.*
import kotlinx.android.synthetic.main.widget_avatar_with_name_horizontal.view.*
import javax.inject.Inject


class BrowseActivity : AppCompatActivity() {
    @Inject
    lateinit var contentGridFragment: ContentGridFragment

    @Inject
    lateinit var contentGroupedByGenreFragment: ContentGroupedByGenreFragment

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sidebarViewModel: SidebarViewModel
    lateinit var preferences: SharedPreferences
    lateinit var currentProfile: Profile

    //Views
    private val contentContainer = R.id.content_container
    private val fragmentContainer = R.id.fragment_container_browse
    private val fragmentManager = supportFragmentManager
    lateinit var profilesView: LinearLayoutCompat
    lateinit var devicesView: LinearLayoutCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get Shared Preferences values
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!getIsProfileSet())
            return
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)

        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        setContentView(R.layout.activity_browse)
        addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName, false)

        //Find needed views
        profilesView = findViewById(R.id.sidebar_profiles)
        devicesView = findViewById(R.id.sidebar_devices_container)

        //Observers
        sidebarViewModel.error.observe(this) { data ->
            Toast.makeText(applicationContext, data, Toast.LENGTH_SHORT)
        }
        sidebarViewModel.isOpenProfiles.observe(this) { data ->
            if (data) {
                buildProfileViews()
            } else {
                profilesView.removeAllViews()
            }
        }
        sidebarViewModel.isOpenDevices.observe(this) { data ->
            if (data) {
                buildDevicesView()
            } else {
                devicesView.removeAllViews()
            }
        }
        sidebarViewModel.notificationsCount.observe(this) { data ->
            findViewById<AppCompatTextView>(R.id.sidebar_notification_count).text = data.toString()
        }

        //Sidebar setup
        val activityLayout = findViewById<DrawerLayout>(R.id.activity_browse)
        val toggle = ActionBarDrawerToggle(
            this,
            activityLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        activityLayout.addDrawerListener(toggle)
        toggle.syncState()
        activityLayout.closeDrawer(Gravity.START)
        activityLayout.sidebar_logout.setOnClickListener {
            logout()
        }
        if (currentProfile.id != -1) {
            activityLayout.sidebar_avatar_active.setOnClickListener {
                sidebarViewModel.clickedProfiles()
            }
        }
        activityLayout.sidebar_notification.setOnClickListener {
            navigateToOption(NotificationFragment::class.simpleName!!)
        }
        activityLayout.sidebar_devices.setOnClickListener {
            sidebarViewModel.clickedDevices()
        }
        activityLayout.sidebar_help.setOnClickListener {
            navigateToOption(HelpFragment::class.simpleName!!)
        }
        activityLayout.sidebar_payment.setOnClickListener {
            navigateToOption(PaymentFragment::class.simpleName!!)
        }
        activityLayout.sidebar_account.setOnClickListener {
            redirectToProfile()
        }
        activityLayout.sidebar_avatar_name.text = currentProfile.name
        picasso.load(currentProfile.avatar.name)
            .config(Bitmap.Config.RGB_565)
            .into(activityLayout.sidebar_avatar_active.widget_avatar)

        //Bottom navigation bar setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navbar)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navbar_serialized -> {
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    addOrReplaceFragment(SerializedFragment(), SerializedFragment::class.simpleName, false)
                    //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_home -> {
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName, false)
                    //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_movie -> {
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    addOrReplaceFragment(MovieFragment(), MovieFragment::class.simpleName, false)
                    //Force ContentGroupedByGenreFragment to run onCreateView with MovieFragment now as main view
                    refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navbar_search -> {
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    addOrReplaceFragment(SearchFragment(), SearchFragment::class.simpleName, false)
                    //Force ContentGroupedByGenreFragment to run onCreateView with SearchFragment now as main view
                    refreshFragmentByTag(contentGridFragment::class.simpleName)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getIsProfileSet()
    }

    override fun onBackPressed() {
        fragmentManager.popBackStack()
    }

    private fun getIsProfileSet(): Boolean {
        val currentProfileString = preferences.getString(BuildConfig.PREFS_PROFILE, "")
        if (currentProfileString.isNullOrEmpty()) {
            navigateToOption(ProfileFragment::class.simpleName!!)
            finish()
            return false
        }
        return true
    }

    private fun buildProfileViews() {
        val marginItems = resources.getDimensionPixelSize(R.dimen.sidebar_avatar_margin_top)
        var params = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.sidebar_avatar_height)
        )
        params.topMargin = marginItems
        //Inflate profile views and "edit profiles" button
        for (profile in sidebarViewModel.profiles) {
            if (profile.id != currentProfile.id) {
                val avatarView = layoutInflater.inflate(R.layout.widget_avatar_with_name_horizontal, null)
                avatarView.id = profile.id!!
                avatarView.widget_avatar_name.text = profile.name
                avatarView.widget_avatar.borderWidth = 0F
                //On click of profile save selection in Shared Preferences and restart Activity
                avatarView.setOnClickListener {
                    preferences.edit().putString(BuildConfig.PREFS_PROFILE, Gson().toJson(profile)).commit()
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navbar)
                    bottomNavigationView.menu.findItem(R.id.navbar_home).isChecked = true
                    recreate()
                }
                avatarView.layoutParams = params
                profilesView.addView(avatarView)
                picasso.load(profile.avatar.name)
                    .config(Bitmap.Config.RGB_565)
                    .into(profilesView.findViewById<LinearLayoutCompat>(profile.id).widget_avatar)
            }
        }
        val editProfilesView = layoutInflater.inflate(R.layout.sidebar_edit_profile, null)
        params = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        params.apply {
            topMargin = marginItems
            bottomMargin = marginItems
        }
        editProfilesView.layoutParams = params
        editProfilesView.setOnClickListener {
            navigateToOption(ProfileFragment::class.simpleName!!)
        }
        profilesView.addView(editProfilesView)
    }

    private fun buildDevicesView() {
        val marginItems = resources.getDimensionPixelSize(R.dimen.sidebar_avatar_margin_top)
        val params = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = marginItems
        for (device in sidebarViewModel.devices) {
            val deviceView = layoutInflater.inflate(R.layout.sidebar_device, null)
            deviceView.sidebar_device_name.text = device.name
            deviceView.sidebar_device_deauth.setOnClickListener {
                //Confirmation dialog
                AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                    .setMessage(R.string.confirmation_delete_device)
                    .setPositiveButton("Confirmar") { _, _ ->
                        sidebarViewModel.clickedDeauth(device)
                    }
                    .setNegativeButton("Cancelar", null)
                    .create().show()
            }
            deviceView.layoutParams = params
            devicesView.addView(deviceView)
        }
        val deleteAllView = layoutInflater.inflate(R.layout.sidebar_delete_all_devices, null)
        deleteAllView.setOnClickListener {
            //Confirmation dialog
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setMessage(R.string.confirmation_delete_devices)
                .setPositiveButton("Confirmar") { _, _ ->
                    sidebarViewModel.deauthAllDevices()
                }
                .setNegativeButton("Cancelar", null)
                .create().show()
        }
        devicesView.addView(deleteAllView)
        //Add separator at bottom of list of devices
        val separatorView = layoutInflater.inflate(R.layout.sidebar_device_separator, null)
        params.bottomMargin = marginItems
        separatorView.layoutParams = params
        devicesView.addView(separatorView)
    }

    fun logout() {
        preferences.edit().remove(BuildConfig.PREFS_COOKIES).remove(BuildConfig.PREFS_COOKIES).commit()
        navigateToAuthentication()
    }

    //Fragment transactions
    private fun addOrReplaceFragment(fragment: Fragment, tag: String?, addToBackStack: Boolean = true) {
        val fragmentInFragmentManager = fragmentManager.findFragmentByTag(tag)
        if (fragmentInFragmentManager == null) {
            fragmentManager.beginTransaction().apply {
                add(fragmentContainer, fragment, tag)
                if (addToBackStack) {
                    addToBackStack(tag)
                }
            }.commit()
            return
        }

        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            val currentFragmentTag = fragmentManager.getBackStackEntryAt(backStackEntryCount - 1).name
            if (currentFragmentTag == tag) {
                return
            }
        }
        fragmentManager.beginTransaction().apply {
            replace(fragmentContainer, fragment, tag)
            if (addToBackStack) {
                addToBackStack(tag)
            }
        }.commit()
    }

    fun navigateToPreview(contentId: Int, contentTypeId: Int) {
        //Set contentId and ContentType for Preview
        fragmentManager.setFragmentResult(
            "preview",
            bundleOf("contentId" to contentId, "contentType" to ContentType.from(contentTypeId))
        )
        addOrReplaceFragment(PreviewFragment(), PreviewFragment::class.simpleName)
    }

    fun changeContentGrid(contents: List<Content>) {
        val fragment = fragmentManager.findFragmentByTag(ContentGridFragment::class.simpleName)
        if (fragment == null || !fragment.isVisible) {
            fragmentManager.beginTransaction()
                .replace(contentContainer, contentGridFragment, ContentGridFragment::class.simpleName).commit()
        }
        contentGridFragment.updateContents(contents)
    }

    fun changeContentGroupedByGenre(contents: List<ContentGroupedByGenre>) {
        val fragment = fragmentManager.findFragmentByTag(ContentGroupedByGenreFragment::class.simpleName)
        if (fragment == null || !fragment.isVisible || !fragment.isDetached) {
            fragmentManager.beginTransaction()
                .replace(
                    contentContainer,
                    contentGroupedByGenreFragment,
                    ContentGroupedByGenreFragment::class.simpleName
                ).commit()
        }
        contentGroupedByGenreFragment.updateContents(contents)
    }

    private fun refreshFragmentByTag(tag: String?) {
        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit()
        }
    }

    private fun detachFragmentByTag(tag: String?) {
        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentManager.beginTransaction().detach(fragment).commit()
        }
    }

    // Activity navigation
    fun navigateToPlayer(content: Content, elapsed: Int = -1) {
        val intent = Intent(this, PlayerActivity::class.java)
        if (elapsed != -1) {
            intent.putExtra("elapsed", elapsed)
        }
        intent.putExtra("content", Gson().toJson(content))
        startActivity(intent)
    }

    fun navigateToPlayer(content: Content, seasonIndex: Int, episodeIndex: Int, elapsed: Int = -1) {
        val intent = Intent(this, PlayerActivity::class.java)
        if (elapsed != -1) {
            intent.putExtra("elapsed", elapsed)
        }
        intent.putExtra("content", Gson().toJson(content))
        intent.putExtra("episodeIndex", episodeIndex)
        intent.putExtra("seasonIndex", seasonIndex)
        startActivity(intent)
    }

    fun navigateToAuthentication() {
        val intent = Intent(applicationContext, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun navigateToOption(fragmentToLoad: String) {
        val intent = Intent(this, OptionActivity::class.java)
        intent.putExtra("FRAGMENT", fragmentToLoad)
        //Send information about where the intent came from
        intent.putExtra("ORIGIN", this::class.simpleName)
        startActivity(intent)
    }

    private fun redirectToProfile() {
        val cookies = preferences.getStringSet(BuildConfig.PREFS_COOKIES, hashSetOf())
        //Find authorization cookie and extract token needed to redirect to profile
        if (cookies != null && cookies.isNotEmpty()) {
            for (cookie in cookies) {
                if (cookie.startsWith("Authorization")) {
                    val pattern = "=.*;".toRegex()
                    val matchResult = pattern.find(cookie)
                    if (matchResult != null) {
                        val match = matchResult.value
                        val token = match.substring(1, match.length - 1)
                        val uri = Uri.parse("https://cinemagold.online/profile_redirect/$token")
                        val i = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(i)
                        break
                    }
                }
            }
        }
    }
}
