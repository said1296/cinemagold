package app.cinemagold.ui.browse.movie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.serialized.FilterCarrouselRVA
import kotlinx.android.synthetic.main.fragment_movie.view.*
import javax.inject.Inject
import javax.inject.Named


class MovieFragment : Fragment() {
    @Inject
    lateinit var viewModel: MovieViewModel
    @Inject
    @Named("genre")
    lateinit var genreFilterCarrouselRVA : FilterCarrouselRVA
    @Inject
    @Named("year")
    lateinit var yearFilterCarrouselRVA: FilterCarrouselRVA

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
        //Observers
        viewModel.error.observe(this){data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentGroupedByGenre.observe(this) {data ->
            (activity as BrowseActivity).changeContentGroupedByGenre(data)
        }
        viewModel.genres.observe(this){data ->
            genreFilterCarrouselRVA.setDataset(data)
            genreFilterCarrouselRVA.selectedPosition = 0
        }
        viewModel.contentGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGrid(data)
        }
        viewModel.years.observe(this){years ->
            yearFilterCarrouselRVA.setDataset(years)
            yearFilterCarrouselRVA.selectedPosition = 0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_movie, container, false)

        //Handle views for TV
        if((activity as BrowseActivity).isTelevision){
            rootView.movie_header.visibility = View.GONE
            requireActivity().findViewById<AppCompatImageButton>(R.id.navbar_filter).setOnClickListener {
                handleFilterClick()
            }
        }
        else
            rootView.movie_header_filter.setOnClickListener {
                handleFilterClick()
            }

        //Recycler views
        rootView.movie_recycler_genres.apply {
            layoutManager = LinearLayoutManager(this@MovieFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreFilterCarrouselRVA
            addItemDecoration(genreFilterCarrouselRVA.itemDecoration)
        }
        rootView.movie_recycler_years.apply {
            layoutManager = LinearLayoutManager(this@MovieFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = yearFilterCarrouselRVA
            addItemDecoration(genreFilterCarrouselRVA.itemDecoration)
        }
        genreFilterCarrouselRVA.clickHandler = { genre ->
            viewModel.selectedGenre(genre)
        }
        yearFilterCarrouselRVA.clickHandler = { year ->
            viewModel.selectedYear(year)
        }

        return rootView
    }

    // UI

    private fun handleFilterClick(){
        if(view!!.movie_recycler_years.visibility == View.VISIBLE) {
            view!!.movie_recycler_years.visibility = View.GONE
            yearFilterCarrouselRVA.selectedPosition = 0
        }
        else view!!.movie_recycler_years.visibility = View.VISIBLE
    }

}
