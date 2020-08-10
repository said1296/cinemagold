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
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.ui.option.OptionActivity
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile_create.view.*
import kotlinx.android.synthetic.main.item_content_vertical.view.*
import kotlinx.android.synthetic.main.widget_avatar.view.*
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
            viewModel.receivedIsEdit(bundle.getBoolean("IS_EDIT")) { buildEdit() }
        }

        //Observers
        viewModel.error.observe(this){data ->
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        }
        viewModel.isSuccessful.observe(this){data ->
            if(data){
                (activity as OptionActivity).navigateToBrowse()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile_create, container, false)
        avatarView = rootView.profile_create_avatar.widget_profile_avatar_edit_avatar as CircularImageView

        rootView.profile_create_avatar.setOnClickListener {
            childFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_in_bottom)
                .add(R.id.profile_create_fragment_container, avatarGridFragment, AvatarGridFragment::class.simpleName).commit()
        }
        rootView.profile_create_submit.setOnClickListener {
            viewModel.submit(rootView.profile_create_form)
        }
        return rootView
    }

    fun buildEdit(){
        picasso.load(viewModel.profile.avatar.name)
            .config(Bitmap.Config.RGB_565)
            .into(avatarView)
        view!!.profile_create_name.setText(viewModel.profile.name)
        view!!.profile_create_name.requestFocus()
    }

    fun selectedAvatar(avatar: IdAndName){
        viewModel.selectedAvatar(avatar)
        picasso.load(avatar.name)
            .config(Bitmap.Config.RGB_565)
            .into(avatarView)
        childFragmentManager.beginTransaction().remove(avatarGridFragment).commit()
    }
}