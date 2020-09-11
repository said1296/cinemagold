package app.cinemagold.ui.browse.serialized

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.browse.BrowseActivity
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
        viewModel.initialize()
        //Observers
        viewModel.error.observe(this){ data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentGroupedByGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGroupedByGenre(data)
        }
        viewModel.genres.observe(this){data ->
            genreRVA.setDataset(data)
        }
        viewModel.contentTypes.observe(this){
            if((activity as BrowseActivity).isTelevision)
                buildContentTypesButtons()
            else
                buildContentTypesSpinner()
        }
        viewModel.contentGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGrid(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_serialized, container, false)

        //Handle views for TV
        if((activity as BrowseActivity).isTelevision)
            rootView.serialized_header.visibility = View.GONE

        //Reset selected position
        genreRVA.selectedPosition = 0

        serializedFragmentLayout = rootView.serialized

        //Recycler views
        rootView.serialized_recycler_genres.apply {
            layoutManager = LinearLayoutManager(this@SerializedFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreRVA
        }
        genreRVA.clickHandler = {genre ->
            viewModel.selectedGenre(genre)
        }

        return rootView
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

    private fun buildContentTypesButtons(){
        val contentTypes = viewModel.contentTypeSpinnerItems
        val contentTypesContainer = serializedFragmentLayout.serialized_content_types
        for ((index, contentType) in contentTypes.withIndex()){
            val contentTypeButton = layoutInflater.inflate(R.layout.item_content_type, contentTypesContainer, false) as TextView
            contentTypeButton.text = contentType
            if(index == 0){
                contentTypeButton.setTextColor(
                    ContextCompat.getColorStateList(requireContext(), R.color.light_focused_brand)
                )
            }
            contentTypeButton.setOnClickListener {
                for(viewIndex in 0 until contentTypesContainer.childCount)
                    (contentTypesContainer.getChildAt(viewIndex) as TextView).
                        setTextColor(
                            ContextCompat.getColorStateList(requireContext(), R.color.light_dark_focused_brand)
                        )
                contentTypeButton.setTextColor(
                    ContextCompat.getColorStateList(requireContext(), R.color.light_focused_brand)
                )
                viewModel.selectedContentType(index)
            }
            contentTypesContainer?.addView(contentTypeButton)
        }
    }
}
