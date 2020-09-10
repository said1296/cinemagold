package app.cinemagold.ui.browse.movie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.serialized.GenreRVA
import kotlinx.android.synthetic.main.fragment_movie.view.*
import javax.inject.Inject


class MovieFragment : Fragment() {
    @Inject
    lateinit var viewModel: MovieViewModel
    @Inject
    lateinit var genreRVA : GenreRVA

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
            genreRVA.setDataset(data)
        }
        viewModel.contentGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGrid(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_movie, container, false)
        //Handle views for TV
        if((activity as BrowseActivity).isTelevision)
            rootView.movie_header.visibility = View.GONE

        //Reset selected position
        genreRVA.selectedPosition = 0

        //Recycler views
        rootView.movie_recycler_genres.apply {
            layoutManager = LinearLayoutManager(this@MovieFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreRVA
            addItemDecoration(genreRVA.itemDecoration)
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

}
