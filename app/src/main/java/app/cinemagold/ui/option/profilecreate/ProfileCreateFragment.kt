package app.cinemagold.ui.option.profilecreate

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import app.cinemagold.BuildConfig
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.ui.common.CircularImageView
import app.cinemagold.ui.common.ContentItemTarget
import app.cinemagold.ui.option.OptionActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile_create.view.*
import kotlinx.android.synthetic.main.widget_avatar_edit.view.*
import javax.inject.Inject

class ProfileCreateFragment : Fragment() {
    @Inject
    lateinit var viewModel: ProfileCreateViewModel

    @Inject
    lateinit var picasso: Picasso
    lateinit var avatarView: CircularImageView
    val avatarGridFragment = AvatarGridFragment()

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Listen for contentId set on Home fragment
        setFragmentResultListener(this::class.simpleName!!) { _, bundle ->
            viewModel.receivedProfileString(bundle.getString(RESULT_SELECTED_PROFILE)) { buildEdit() }
        }

        //Observers
        viewModel.error.observe(this) { data ->
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        }
        viewModel.isSuccessfulCreate.observe(this) { data ->
            if (data) {
                (activity as OptionActivity).navigateToBrowse()
            }
        }
        viewModel.isSuccessfulDelete.observe(this) { data ->
            if (data.first) {
                if(data.second) {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
                    preferences.remove(BuildConfig.PREFS_PROFILE).apply()
                }
                (activity as OptionActivity).loadFragment()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile_create, container, false)
        avatarView = rootView.widget_avatar_edit.widget_profile_avatar_edit_avatar as CircularImageView

        rootView.profile_create_widget_avatar_edit.setOnClickListener {
            childFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_in_bottom)
                .add(R.id.profile_create_fragment_container, avatarGridFragment, AvatarGridFragment::class.simpleName)
                .commit()
        }
        rootView.profile_create_submit.setOnClickListener {
            viewModel.submit(rootView.profile_create_form)
        }
        rootView.profile_create_name.requestFocus()
        return rootView
    }

    fun buildEdit() {
        buildAvatar(viewModel.profile.avatar)
        view!!.profile_create_name.setText(viewModel.profile.name)
        view!!.profile_delete.visibility = View.VISIBLE
        view!!.profile_delete.setOnClickListener {
            viewModel.deleteProfile()
        }
    }

    fun selectedAvatar(avatar: IdAndName) {
        viewModel.selectedAvatar(avatar)
        buildAvatar(avatar)
        childFragmentManager.beginTransaction().remove(avatarGridFragment).commit()
        requireView().profile_create_name.requestFocus()
    }

    private fun buildAvatar(avatar: IdAndName) {
        val target = ContentItemTarget(resources) { stateListDrawable ->
            avatarView.setImageDrawable(stateListDrawable)
        }
        //Keep strong reference to target with a tag to avoid garbage collection
        avatarView.tag = target
        picasso.load(avatar.name)
            .config(Bitmap.Config.RGB_565)
            .into(target)
    }

    companion object {
        const val RESULT_IS_EDIT = "IS_EDIT"
        const val RESULT_SELECTED_PROFILE = "SELECTED_PROFILE"
    }
}
