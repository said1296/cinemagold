package app.cinemagold.ui.option.profile

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.common.ContentItemTarget
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.profilecreate.ProfileCreateFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.widget_avatar
import kotlinx.android.synthetic.main.widget_avatar_edit_with_name.view.*
import kotlinx.android.synthetic.main.widget_avatar_with_name.view.widget_avatar_name
import javax.inject.Inject


class ProfileFragment : Fragment() {
    @Inject
    lateinit var viewModel: ProfileViewModel
    @Inject
    lateinit var picasso: Picasso
    var avatarViews: MutableList<View> = mutableListOf()
    lateinit var preferences : SharedPreferences

    private val typedValue = TypedValue()

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startedFragment()

        val theme : Resources.Theme = ContextThemeWrapper(context, R.style.AppTheme).theme
        theme.resolveAttribute(R.attr.light, typedValue, true)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        //Observers
        viewModel.error.observe(this){data ->
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        }
        viewModel.profiles.observe(this){data ->
            buildAvatars()
        }
        viewModel.isSuccessful.observe(this){
            if(viewModel.isEdit){
                setFragmentResult(ProfileCreateFragment::class.simpleName!!, bundleOf("IS_EDIT" to true))
                (activity as OptionActivity).addOrReplaceFragment(ProfileCreateFragment(), ProfileCreateFragment::class.simpleName)
            }else{
                (activity as OptionActivity).navigateToBrowse()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentProfileString = preferences.getString(BuildConfig.PREFS_PROFILE, "")
        val isEdit = (activity as OptionActivity).getIsEdit()
        viewModel.receivedIsEdit(isEdit && !currentProfileString.isNullOrEmpty())

        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        if(viewModel.isEdit){
            rootView.profile_avatar_first_flipper.displayedChild = 1
            rootView.profile_avatar_second_flipper.displayedChild = 1
            rootView.profile_avatar_third_flipper.displayedChild = 1
            rootView.profile_kids.visibility = View.GONE
        }
        avatarViews.apply {
            add(rootView.profile_avatar_first_flipper.currentView)
            add(rootView.profile_avatar_second_flipper.currentView)
            add(rootView.profile_avatar_third_flipper.currentView)
        }
        rootView.profile_kids.setOnClickListener {
            viewModel.selectedKids()
        }
        return rootView
    }

    override fun onStart() {
        viewModel.startedFragment()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        view?.profile_avatar_first_flipper?.profile_avatar_first?.requestFocus()
    }

    private fun buildAvatars(){
        val profiles = viewModel.profiles.value
        val profilesSize = profiles!!.size
        for((index, avatarView) in avatarViews.withIndex()){
            //Build focuses for TV
            if(context!!.resources.getBoolean(R.bool.isTelevision)){
                avatarView.apply {
                    when(index){
                        0 -> {
                            nextFocusLeftId = avatarView.id
                            nextFocusRightId = avatarViews[index+1].id
                        }
                        avatarViews.size-1 -> {
                            nextFocusLeftId = avatarViews[index-1].id
                            nextFocusRightId = avatarView.id
                        }
                        else -> {
                            nextFocusLeftId = avatarViews[index-1].id
                            nextFocusRightId = avatarViews[index+1].id
                        }
                    }
                    nextFocusUpId = avatarView.id
                    nextFocusDownId = R.id.profile_kids
                }
            }

            if(viewModel.isEdit && index>=profilesSize){
                avatarView.widget_avatar_edit_icon.visibility = View.GONE
            }
            if(index>=profilesSize){
                avatarView.widget_avatar_name.text = ""
                avatarView.widget_avatar.setImageResource(R.drawable.bg_profile_empty)
                avatarView.widget_avatar.borderColor = ContextCompat.getColor(requireContext(), typedValue.resourceId)
                avatarView.setOnClickListener {
                    setFragmentResult(ProfileCreateFragment::class.simpleName!!, bundleOf("IS_EDIT" to false))
                    (activity as OptionActivity).addOrReplaceFragment(ProfileCreateFragment(), ProfileCreateFragment::class.simpleName)
                }
            }else{
                val target = ContentItemTarget(resources) { stateListDrawable ->
                    avatarView.widget_avatar.setImageDrawable(stateListDrawable)
                }
                //Keep strong reference to target with a tag to avoid garbage collection
                avatarView.widget_avatar.widget_avatar.tag = target
                picasso.load(profiles[index].avatar.name)
                    .config(Bitmap.Config.RGB_565)
                    .into(target)
                avatarView.widget_avatar_name.text = profiles[index].name
                avatarView.setOnClickListener {
                    viewModel.selectedProfile(index)
                }
            }
        }
        avatarViews[0].requestFocus()
    }
}
