package app.cinemagold.ui.authentication.confirmemail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.cinemagold.R
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.authentication.login.LoginFragment
import kotlinx.android.synthetic.main.fragment_confirm_email.view.*

class ConfirmEmailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_confirm_email, container, false)
        rootView.confirm_email_continue.setOnClickListener {
            (activity as AuthenticationActivity).addOrReplaceFragment(LoginFragment(), LoginFragment::class.simpleName)
        }
        return rootView
    }
}
