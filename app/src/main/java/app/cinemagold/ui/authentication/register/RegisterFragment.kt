package app.cinemagold.ui.authentication.register

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
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
        viewModel.countrySpinnerItems.observe(this){data ->
            buildCountrySpinner()
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
        rootView.register_submit.setOnClickListener {
            viewModel.submit(rootView.register_form)
        }
        return rootView
    }

    private fun buildCountrySpinner(){
        val countrySpinnerItems = viewModel.countrySpinnerItems.value
        val adapter = ArrayAdapter<String>(context!!.applicationContext, R.layout.spinner_country, countrySpinnerItems!!)
        adapter.setDropDownViewResource(R.layout.spinner_country_item)
        view!!.register_spinner_country.adapter = adapter
        view!!.register_spinner_country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //Grey out first option
                val typedValue = TypedValue()
                val theme : Resources.Theme = ContextThemeWrapper(context, R.style.AppTheme).theme
                if(p2 == 0){
                    theme.resolveAttribute(R.attr.lightDark, typedValue, true)
                }else{
                    theme.resolveAttribute(R.attr.light, typedValue, true)
                }
                (view!!.spinner_country_selected as TextView).setTextColor(context!!.getColor(typedValue.resourceId))
                for (drawable in (view!!.spinner_country_selected as TextView).compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter = PorterDuffColorFilter(context!!.getColor(typedValue.resourceId), PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        }
    }
}
