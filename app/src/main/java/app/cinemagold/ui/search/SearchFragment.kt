package app.cinemagold.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.MainActivity
import app.cinemagold.ui.serialized.GenreRVA
import kotlinx.android.synthetic.main.fragment_search.view.*
import javax.inject.Inject


class SearchFragment : Fragment() {
    @Inject
    lateinit var viewModel: SearchViewModel
    @Inject
    lateinit var genreRVA : GenreRVA

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Observers
        viewModel.error.observe(this){data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentSearch.observe(this) {data ->
            (activity as MainActivity).changeContentGrid(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        view.search_input.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.inputSearch(p0.toString())
            }

        })

        return view
    }


    override fun onStop() {
        super.onStop()
    }

}