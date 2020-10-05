package app.cinemagold.ui.browse

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
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
import app.cinemagold.ui.common.ContentItemTarget
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.help.HelpFragment
import app.cinemagold.ui.option.notification.NotificationFragment
import app.cinemagold.ui.option.payment.PaymentFragment
import app.cinemagold.ui.option.profile.ProfileFragment
import app.cinemagold.ui.player.PlayerActivity
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.menu.view.*
import kotlinx.android.synthetic.main.menu_avatar_with_name.view.*
import kotlinx.android.synthetic.main.menu_device.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.*
import javax.inject.Inject


class BrowseActivity : AppCompatActivity() {
    @Inject
    lateinit var contentGridFragment: ContentGridFragment

    @Inject
    lateinit var contentGroupedByGenreFragment: ContentGroupedByGenreFragment

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var menuViewModel: MenuViewModel
    lateinit var preferences: SharedPreferences
    lateinit var currentProfile: Profile
    var isTelevision: Boolean = false

    //Views
    private val contentContainer = R.id.content_container
    private val fragmentContainer = R.id.fragment_container_browse
    private val fragmentManager = supportFragmentManager
    lateinit var profilesView: LinearLayoutCompat
    lateinit var devicesView: LinearLayoutCompat
    lateinit var activityView: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        isTelevision = resources.getBoolean(R.bool.isTelevision)
        requestedOrientation =
            if (isTelevision) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }else{
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        super.onCreate(savedInstanceState)
        //Get Shared Preferences values
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!getIsProfileSet())
            return
        currentProfile = Gson().fromJson(preferences.getString(BuildConfig.PREFS_PROFILE, ""), Profile::class.java)

        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        setContentView(R.layout.activity_browse)
        val fragment: Fragment? = when(intent.getStringExtra("FRAGMENT")){
            PreviewFragment::class.simpleName -> {
                //Set contentId and ContentType for Preview
                fragmentManager.setFragmentResult(
                    "preview",
                    bundleOf("contentId" to intent.getIntExtra("CONTENT_ID", 0),
                        "contentType" to ContentType.from(intent.getIntExtra("CONTENT_TYPE", 0)))
                )
                findViewById<LinearLayoutCompat>(R.id.navbar).visibility = View.GONE
                PreviewFragment()
            }
            else -> {
                null
            }
        }
        println("FRAGMENT -> " + fragment)
        addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName, false)
        if(fragment != null) addOrReplaceFragment(fragment, fragment::class.simpleName)

        //Find needed views
        profilesView = findViewById(R.id.menu_profiles)
        devicesView = findViewById(R.id.menu_devices_container)

        //Observers
        menuViewModel.error.observe(this) { data ->
            Toast.makeText(applicationContext, data, Toast.LENGTH_SHORT)
        }
        menuViewModel.isOpenProfiles.observe(this) { data ->
            if (data) {
                buildProfileViews()
            } else {
                if(!isTelevision)
                    profilesView.visibility = View.GONE
            }
        }
        menuViewModel.isOpenDevices.observe(this) { data ->
            if (data) {
                buildDevicesView()
            } else {
                devicesView.removeAllViews()
            }
        }
        menuViewModel.notificationsCount.observe(this) { count ->
            if(count > 0){
                val menuNotificationView = findViewById<LinearLayoutCompat>(R.id.menu_notification)
                menuNotificationView.menu_notification_count.text = count.toString()
                menuNotificationView.menu_notification_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            }
        }

        //Menu setup
        activityView = findViewById(R.id.activity_browse)
        val toggle = object: ActionBarDrawerToggle(this, activityView, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                activityView.menu_devices.requestFocus()
            }
        }
        activityView.addDrawerListener(toggle)
        toggle.syncState()
        activityView.closeDrawer(Gravity.START)
        activityView.menu_logout.setOnClickListener {
            logout()
        }
        if (currentProfile.id != -1) {
            if(!isTelevision)
                activityView.menu_avatar_active.setOnClickListener {
                    menuViewModel.clickedProfiles()
                }
        }
        activityView.menu_profiles_edit_profiles.setOnClickListener {
            navigateToOption(ProfileFragment::class.simpleName!!, true)
        }
        activityView.menu_notification.setOnClickListener {
            navigateToOption(NotificationFragment::class.simpleName!!)
        }
        activityView.menu_devices.setOnClickListener {
            menuViewModel.clickedDevices()
        }
        activityView.menu_help.setOnClickListener {
            navigateToOption(HelpFragment::class.simpleName!!)
        }
        activityView.menu_payment.setOnClickListener {
            navigateToOption(PaymentFragment::class.simpleName!!)
        }
        activityView.menu_account.setOnClickListener {
            redirectToProfile()
        }
        activityView.menu_avatar_name?.text = currentProfile.name

        val target = ContentItemTarget(resources) { stateListDrawable ->
            activityView.menu_avatar_active.widget_avatar.setImageDrawable(stateListDrawable)
        }
        //Keep strong reference to target with a tag to avoid garbage collection
        activityView.menu_avatar_active.widget_avatar.tag = target
        picasso.load(currentProfile.avatar.name)
            .config(Bitmap.Config.RGB_565)
            .into(target)
        activityView.menu_avatar_active.widget_avatar.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        getIsProfileSet()
    }

    override fun onBackPressed() {
        if (activityView.isDrawerOpen(GravityCompat.START))
            activityView.closeDrawer(GravityCompat.START)
        else{
            findViewById<LinearLayoutCompat>(R.id.navbar).visibility = View.VISIBLE
            fragmentManager.popBackStack()
        }
    }

    //This function is indicated in the XML layout for navbar under the attr onClick: of each item
    fun handleNavbarClick(view: View) {
        when (view.id) {
            R.id.navbar_serialized -> {
                detachFragmentByTag(HomeFragment::class.simpleName)
                detachFragmentByTag(MovieFragment::class.simpleName)
                addOrReplaceFragment(SerializedFragment(), SerializedFragment::class.simpleName, false)
                //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
            }
            R.id.navbar_home -> {
                detachFragmentByTag(MovieFragment::class.simpleName)
                detachFragmentByTag(SerializedFragment::class.simpleName)
                addOrReplaceFragment(HomeFragment(), HomeFragment::class.simpleName, false)
                //Force ContentGroupedByGenreFragment to run onCreateView with SerializedFragment now as main view
                refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
            }
            R.id.navbar_movie -> {
                detachFragmentByTag(HomeFragment::class.simpleName)
                detachFragmentByTag(SerializedFragment::class.simpleName)
                addOrReplaceFragment(MovieFragment(), MovieFragment::class.simpleName, false)
                //Force ContentGroupedByGenreFragment to run onCreateView with MovieFragment now as main view
                refreshFragmentByTag(contentGroupedByGenreFragment::class.simpleName)
            }
            R.id.navbar_search -> {
                detachFragmentByTag(HomeFragment::class.simpleName)
                detachFragmentByTag(SerializedFragment::class.simpleName)
                detachFragmentByTag(MovieFragment::class.simpleName)
                addOrReplaceFragment(SearchFragment(), SearchFragment::class.simpleName, false)
                //Force ContentGroupedByGenreFragment to run onCreateView with SearchFragment now as main view
                refreshFragmentByTag(contentGridFragment::class.simpleName)
            }
            R.id.navbar_menu -> {
                menuViewModel.clickedProfiles()
                activityView.openDrawer(GravityCompat.START)
            }
            R.id.menu_avatar_active -> {
                navigateToOption(ProfileFragment::class.simpleName!!, false)
            }
            else -> {
            }
        }
    }

    private fun getIsProfileSet(): Boolean {
        val currentProfileString = preferences.getString(BuildConfig.PREFS_PROFILE, "")
        if (currentProfileString.isNullOrEmpty()) {
            navigateToOption(ProfileFragment::class.simpleName!!, false)
            finish()
            return false
        }
        return true
    }

    private fun buildProfileViews() {
        profilesView.menu_profiles_avatars.removeAllViews()
        profilesView.visibility = View.VISIBLE
        val marginItems = resources.getDimensionPixelSize(R.dimen.menu_avatar_margin_top)
        val params = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.menu_avatar_height)
        )
        params.apply {
            if (isTelevision) {
                weight = 100f / 10
            } else
                topMargin = marginItems
        }
        //Inflate profile views and "edit profiles" button
        for ((index, profile) in menuViewModel.profiles.withIndex()) {
            if (profile.id != currentProfile.id || isTelevision) {
                val avatarView = layoutInflater.inflate(R.layout.menu_avatar_with_name, null)
                avatarView.id = profile.id!!
                avatarView.widget_avatar_name.text = profile.name
                avatarView.widget_avatar.borderWidth = 0F
                //On click of profile save selection in Shared Preferences and restart Activity
                avatarView.setOnClickListener {
                    preferences.edit().putString(BuildConfig.PREFS_PROFILE, Gson().toJson(profile)).commit()
                    detachFragmentByTag(HomeFragment::class.simpleName)
                    detachFragmentByTag(MovieFragment::class.simpleName)
                    detachFragmentByTag(SerializedFragment::class.simpleName)
                    //TODO: implement select Home in navbar when profile changed
                    recreate()
                }
                avatarView.layoutParams = params
                val target = ContentItemTarget(resources) { stateListDrawable ->
                    avatarView.widget_avatar.setImageDrawable(stateListDrawable)
                }
                //Keep strong reference to target with a tag to avoid garbage collection
                avatarView.widget_avatar.tag = target
                picasso.load(profile.avatar.name)
                    .config(Bitmap.Config.RGB_565)
                    .into(target)

                profilesView.menu_profiles_avatars.addView(avatarView)
            }
        }
    }

    private fun buildDevicesView() {
        val marginItems = resources.getDimensionPixelSize(R.dimen.menu_avatar_margin_top)
        val params = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = marginItems
        for (device in menuViewModel.devices) {
            val deviceView = layoutInflater.inflate(R.layout.menu_device, null)
            deviceView.menu_device_name.text = device.name
            deviceView.menu_device_deauth.setOnClickListener {
                //Confirmation dialog
                AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                    .setMessage(R.string.confirmation_delete_device)
                    .setPositiveButton("Confirmar") { _, _ ->
                        menuViewModel.clickedDeauth(device)
                    }
                    .setNegativeButton("Cancelar", null)
                    .create().show()
            }
            deviceView.layoutParams = params
            devicesView.addView(deviceView)
        }
        val deleteAllView = layoutInflater.inflate(R.layout.menu_delete_all_devices, null)
        deleteAllView.setOnClickListener {
            //Confirmation dialog
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setMessage(R.string.confirmation_delete_devices)
                .setPositiveButton("Confirmar") { _, _ ->
                    menuViewModel.deauthAllDevices()
                }
                .setNegativeButton("Cancelar", null)
                .create().show()
        }
        devicesView.addView(deleteAllView)
        //Add separator at bottom of list of devices
        val separatorView = layoutInflater.inflate(R.layout.menu_device_separator, null)
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
        findViewById<LinearLayoutCompat>(R.id.navbar).visibility = View.GONE
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

    private fun navigateToOption(fragmentToLoad: String, isEdit: Boolean? = null) {
        val intent = Intent(this, OptionActivity::class.java)
        intent.putExtra("FRAGMENT", fragmentToLoad)
        //Send information about where the intent came from
        intent.putExtra("ORIGIN", this::class.simpleName)
        isEdit?.also { intent.putExtra("IS_EDIT", isEdit) }
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
