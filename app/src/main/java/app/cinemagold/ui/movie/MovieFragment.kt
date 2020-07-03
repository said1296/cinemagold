package app.cinemagold.ui.movie

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
import app.cinemagold.ui.MainActivity
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
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
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
