package app.cinemagold.ui.movie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import app.cinemagold.ui.serialized.GenreRVA
import app.cinemagold.ui.serialized.MovieViewModel
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

        //Observers
        viewModel.error.observe(this){data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentGroupedByGenre.observe(this) {data ->
            (activity as MainActivity).changeContentGroupedByGenre(data)
        }
        viewModel.genres.observe(this){data ->
            genreRVA.setDataset(data)
        }
        viewModel.contentGenre.observe(this){data ->
            (activity as MainActivity).changeContentGrid(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("CREATEVIEW FRAGMENT")
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
        //Initial child fragment needs to be refreshed in order to inflate its view because the onStop method of this
        //fragment doesn't travel down and onCreateView is not called.
        (activity as MainActivity).refreshFragmentByTag(ContentGroupedByGenreFragment::class.simpleName)
        //Reset selected position
        genreRVA.selectedPosition = 0

        //Recycler views
        view.movie_recycler_genres.apply {
            layoutManager = LinearLayoutManager(this@MovieFragment.context, LinearLayoutManager.HORIZONTAL, false)
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

}
