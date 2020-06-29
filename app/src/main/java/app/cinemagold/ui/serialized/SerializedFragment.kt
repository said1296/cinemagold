package app.cinemagold.ui.serialized

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.generic.GenericIdAndName
import app.cinemagold.ui.MainActivity
import app.cinemagold.ui.common.fragment.ContentGroupedByGenreFragment
import kotlinx.android.synthetic.main.fragment_serialized.view.*
import javax.inject.Inject


class SerializedFragment : Fragment() {
    @Inject
    lateinit var viewModel: SerializedViewModel
    @Inject
    lateinit var genreRVA : GenreRVA
    lateinit var serializedFragmentLayout: LinearLayoutCompat

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
        viewModel.contentGroupedByGenre.observe(this){data ->
            (activity as MainActivity).changeContentGroupedByGenre(data)
        }
        viewModel.genres.observe(this){data ->
            genreRVA.setDataset(data)
        }
        viewModel.contentTypes.observe(this){
            buildContentTypesSpinner()
        }
        viewModel.contentGenre.observe(this){data ->
            (activity as MainActivity).changeContentGrid(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_serialized, container, false)
        //Initial child fragment needs to be refreshed in order to inflate its view because the onStop method of this
        //fragment doesn't travel down and onCreateView is not called.
        (activity as MainActivity).refreshFragmentByTag(ContentGroupedByGenreFragment::class.simpleName)
        //Reset selected position
        genreRVA.selectedPosition = 0

        serializedFragmentLayout = view.serialized

        //Recycler views
        view.serialized_recycler_genres.apply {
            layoutManager = LinearLayoutManager(this@SerializedFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreRVA
        }
        genreRVA.clickHandler = {genre ->
            viewModel.selectedGenre(genre)
        }

        return view
    }

    override fun onStop() {
        viewModel.stoppedFragment()
        super.onStop()
    }

    private fun buildContentTypesSpinner(){
        val contentTypeSpinnerItems = viewModel.contentTypeSpinnerItems
        val adapter = ArrayAdapter<String>(context!!.applicationContext, R.layout.spinner_content_type, contentTypeSpinnerItems)
        adapter.setDropDownViewResource(R.layout.spinner_content_type_item)
        serializedFragmentLayout.serialized_spinner_content_type.adapter = adapter
        //Set selection so it doesn't fire onItemSelected when the listener is attached
        serializedFragmentLayout.serialized_spinner_content_type.setSelection(0, false)
        serializedFragmentLayout.serialized_spinner_content_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.selectedContentType(p2)
            }
        }
    }

}
