package app.cinemagold.ui.authentication.login

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.option.profile.ProfileFragment
import kotlinx.android.synthetic.main.fragment_login.view.*
import javax.inject.Inject

class LoginFragment : Fragment() {
    @Inject
    lateinit var viewModel: LoginViewModel

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Observers
        viewModel.error.observe(this){ data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.isSuccessful.observe(this){
            (activity as AuthenticationActivity).navigateToOption(ProfileFragment::class.simpleName!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)
        rootView.login_password_edit_text.transformationMethod = PasswordTransformationMethod()
        rootView.login_password_show.setOnClickListener { view ->
            if(rootView.login_password_edit_text.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD){
                rootView.login_password_edit_text.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                view.isSelected = true
                (view as ImageButton).setImageResource(R.drawable.ic_eye_open_24dp)
            }else{
                rootView.login_password_edit_text.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                view.isSelected = false
                (view as ImageButton).setImageResource(R.drawable.ic_eye_closed_24dp)
            }

        }
        rootView.login_submit.setOnClickListener {
            viewModel.submit(rootView.login_email_edit_text.text.toString(), rootView.login_password_edit_text.text.toString())
        }
        return rootView
    }
}
