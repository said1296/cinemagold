package app.cinemagold.ui.authentication.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.cinemagold.R
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.authentication.login.LoginFragment
import app.cinemagold.ui.authentication.register.RegisterFragment
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_in, container, false)
        rootView.sign_in_login.setOnClickListener {
            (activity as AuthenticationActivity).addOrReplaceFragment(LoginFragment(), LoginFragment::class.simpleName)
        }
        rootView.sign_in_register.setOnClickListener {
            (activity as AuthenticationActivity).addOrReplaceFragment(RegisterFragment(), RegisterFragment::class.simpleName)
        }
        return rootView
    }
}
