package app.cinemagold.ui.option.profile

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.observe
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.profilecreate.ProfileCreateFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.widget_avatar
import kotlinx.android.synthetic.main.widget_avatar_edit_with_name.view.*
import kotlinx.android.synthetic.main.widget_avatar_with_name.view.*
import kotlinx.android.synthetic.main.widget_avatar_with_name.view.widget_avatar_name
import javax.inject.Inject


class ProfileFragment : Fragment() {
    @Inject
    lateinit var viewModel: ProfileViewModel
    @Inject
    lateinit var picasso: Picasso
    var avatarViews: MutableList<View> = mutableListOf()

    private val typedValue = TypedValue()

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme : Resources.Theme = ContextThemeWrapper(context, R.style.AppTheme).theme
        theme.resolveAttribute(R.attr.light, typedValue, true)
        super.onCreate(savedInstanceState)

        val origin = (activity as OptionActivity).intent.getStringExtra("ORIGIN")
        if(origin!= AuthenticationActivity::class.simpleName){
            viewModel.receivedIsEdit(true)
        }

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
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        if(viewModel.isEdit){
            rootView.profile_avatar_first_flipper.displayedChild = 1
            rootView.profile_avatar_second_filpper.displayedChild = 1
            rootView.profile_avatar_third_flipper.displayedChild = 1
        }
        avatarViews.apply {
            add(rootView.profile_avatar_first_flipper.currentView)
            add(rootView.profile_avatar_second_filpper.currentView)
            add(rootView.profile_avatar_third_flipper.currentView)
        }
        rootView.profile_kids.setOnClickListener {
            viewModel.selectedKids()
        }
        return rootView
    }

    private fun buildAvatars(){
        val profiles = viewModel.profiles.value
        val profilesSize = profiles!!.size
        for((index, avatarView) in avatarViews.withIndex()){
            if(viewModel.isEdit && index>=profilesSize){
                avatarView.widget_avatar_edit_icon.visibility = View.GONE
            }
            if(index>=profilesSize){
                avatarView.widget_avatar.borderColor = context!!.getColor(typedValue.resourceId)
                avatarView.setOnClickListener {
                    setFragmentResult(ProfileCreateFragment::class.simpleName!!, bundleOf("IS_EDIT" to false))
                    (activity as OptionActivity).addOrReplaceFragment(ProfileCreateFragment(), ProfileCreateFragment::class.simpleName)
                }
            }else{
                picasso.load(profiles[index].avatar.name)
                    .config(Bitmap.Config.RGB_565)
                    .into(avatarView.widget_avatar)
                avatarView.widget_avatar_name.text = profiles[index].name
                avatarView.setOnClickListener {
                    viewModel.selectedProfile(index)
                }
            }
        }
    }
}
