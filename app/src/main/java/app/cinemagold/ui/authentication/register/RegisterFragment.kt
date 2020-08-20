package app.cinemagold.ui.authentication.register

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.authentication.confirmemail.ConfirmEmailFragment
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.spinner_country.view.*
import javax.inject.Inject


class RegisterFragment : Fragment() {
    @Inject
    lateinit var viewModel: RegisterViewModel

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
        viewModel.countrySpinnerItems.observe(this){
            buildCountrySpinnerAuto()
        }
        viewModel.isSuccessful.observe(this){
            (activity as AuthenticationActivity).addOrReplaceFragment(ConfirmEmailFragment(), ConfirmEmailFragment::class.simpleName)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_register, container, false)
        val passwordEditText = rootView.register_password_edit_text
        passwordEditText.transformationMethod = PasswordTransformationMethod()
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        rootView.register_password_show.setOnClickListener { view ->
            if(passwordEditText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD){
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                view.isSelected = true
                (view as ImageButton).setImageResource(R.drawable.ic_eye_open_24dp)
                //For some reason Typeface changes when changing input type so we have to set it manually
                passwordEditText.typeface = rootView.register_email.typeface
            }else{
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                view.isSelected = false
                (view as ImageButton).setImageResource(R.drawable.ic_eye_closed_24dp)
            }

        }
        rootView.register_submit.setOnClickListener {
            viewModel.submit(rootView.register_form)
        }
        return rootView
    }

    private fun buildCountrySpinnerAuto(){
        val autoCompleteView: AutoCompleteTextView
        val arrayAdapter = ArrayAdapter(
            requireActivity(), R.layout.spinner_country_item,
            viewModel.countrySpinnerItems.value!!
        )

        autoCompleteView = view!!.register_country
        autoCompleteView.setAdapter(arrayAdapter)
        autoCompleteView.setOnClickListener { autoCompleteView.showDropDown() }
        autoCompleteView.setOnItemClickListener { adapterView, view, i, l ->
            viewModel.selectedCountry(adapterView.getItemAtPosition(i) as String)
        }
        autoCompleteView.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                autoCompleteView.setText(viewModel.selectedCountry.name)
            }
        }
    }
}
