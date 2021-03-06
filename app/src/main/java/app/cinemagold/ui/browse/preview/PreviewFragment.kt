package app.cinemagold.ui.browse.preview

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.browse.BrowseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_preview.view.*
import kotlinx.android.synthetic.main.preview_info_specific_movie.view.*
import kotlinx.android.synthetic.main.preview_info_specific_serialized.view.*
import kotlinx.android.synthetic.main.widget_button_play.view.*
import javax.inject.Inject

class PreviewFragment : Fragment() {
    @Inject
    lateinit var viewModel: PreviewViewModel

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var episodeRVA: EpisodeRVA
    var contentId: Int = -1
    var seasonIndex: Int = -1
    lateinit var contentType: ContentType
    lateinit var content: Content
    private var currentSeasonIndex: Int = 0

    var isTelevision: Boolean = false

    //Views
    lateinit var infoSpecificContainerView: LinearLayout
    lateinit var titleView: TextView
    lateinit var genreMainView: TextView
    lateinit var genreSecondaryView: TextView
    lateinit var descriptionShortView: TextView
    lateinit var sliderView: ImageView
    lateinit var scoreView: RatingBar
    lateinit var infoSpecificView: View

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isTelevision = resources.getBoolean(R.bool.isTelevision)

        //Listen for contentId set on Home fragment
        setFragmentResultListener("preview") { _, bundle ->
            contentId = bundle.getInt(EXTRA_CONTENT_ID)
            contentType = bundle.get(EXTRA_CONTENT_TYPE) as ContentType
            seasonIndex = bundle.getInt(EXTRA_SEASON_INDEX)
            viewModel.receivedContentIdAndContentType(contentId, contentType)
        }

        //Observers
        viewModel.error.observe(this) { data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.content.observe(this) { data ->
            content = data
            updateCommonView()

            if (viewModel.currentContentType == ContentType.MOVIE) {
                updateInfoSpecificMovieView()
            } else {
                updateInfoSpecificSerializedView()
            }
            infoSpecificView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            infoSpecificContainerView.addView(infoSpecificView)

            if (isTelevision)
                if (viewModel.currentContentType == ContentType.MOVIE)
                    requireView().widget_button_play_movie.apply {
                        requestFocus()
                        setOnClickListener {
                            (activity as BrowseActivity).navigateToPlayer(content)
                        }
                    }
                else
                    requireView().widget_button_play_serialized.apply {
                        requestFocus()
                        setOnClickListener {
                            (activity as BrowseActivity).navigateToPlayer(content)
                        }
                    }
            else
                requireView().widget_button_play.setOnClickListener {
                    (activity as BrowseActivity).navigateToPlayer(content)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preview, container, false)
        infoSpecificContainerView = view.preview_info_specific
        titleView = view.preview_info_title
        genreMainView = view.preview_info_genre_main
        genreSecondaryView = view.preview_info_genre_secondary
        descriptionShortView = view.preview_info_description_short
        sliderView = view.preview_slider
        scoreView = view.preview_info_score

        //Hide keyboard
        val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        return view
    }

    private fun updateCommonView() {
        titleView.text = content.name
        genreMainView.text = content.genreMain.name
        genreSecondaryView.text = content.genreSecondary.name
        descriptionShortView.text = content.descriptionShort
        scoreView.rating = content.score
    }

    //Add information specific to movies
    private fun updateInfoSpecificMovieView() {
        val previewImageSrc = if (resources.getBoolean(R.bool.isTelevision)) content.posterSrc else content.sliderSrc
        picasso.load(previewImageSrc).apply {
            placeholder(R.drawable.bg_dark)
            config(Bitmap.Config.RGB_565)
            fit()
            centerCrop()
            into(sliderView)
        }
        infoSpecificView = layoutInflater.inflate(R.layout.preview_info_specific_movie, null)
        infoSpecificView.preview_info_length.text = content.length
        infoSpecificView.preview_info_director.text = content.movie.director
    }

    //Add information specific to serialized content
    private fun updateInfoSpecificSerializedView() {
        infoSpecificView = layoutInflater.inflate(R.layout.preview_info_specific_serialized, null)

        //Episodes generic_recycler view
        val recyclerViewOrientation = if (isTelevision) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL
        infoSpecificView.preview_recycler_episode.apply {
            adapter = episodeRVA
            layoutManager = LinearLayoutManager(context, recyclerViewOrientation, false)
        }
        episodeRVA.clickHandler = { data ->
            (activity as BrowseActivity).navigateToPlayer(content, currentSeasonIndex, data)
        }
        episodeRVA.isPlayerActivity = false
        episodeRVA.setDataset(content.seasons[currentSeasonIndex].episodes)

        //Season selector
        val seasonSpinnerItems = mutableListOf<String>()
        for (season in content.seasons) {
            seasonSpinnerItems.add("Temporada ${season.number}")
        }
        val adapter = ArrayAdapter<String>(context!!.applicationContext, R.layout.spinner_season, seasonSpinnerItems)
        adapter.setDropDownViewResource(R.layout.spinner_season_item)
        infoSpecificView.spinner_season.adapter = adapter
        var previewImageSrc: String
        var firstSelection = true
        infoSpecificView.spinner_season.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //onItemSelected is called on first run, even if nothing was manually selected
                if (currentSeasonIndex != p2 || firstSelection) {
                    firstSelection = false
                    currentSeasonIndex = p2
                    previewImageSrc =
                        if (resources.getBoolean(R.bool.isTelevision)) content.seasons[currentSeasonIndex].posterSrc else content.seasons[currentSeasonIndex].sliderSrc
                    episodeRVA.setDataset(content.seasons[currentSeasonIndex].episodes)
                    picasso.load(previewImageSrc).apply {
                        placeholder(R.drawable.bg_dark)
                        config(Bitmap.Config.RGB_565)
                        fit()
                        centerCrop()
                        into(sliderView)
                    }
                }
            }
        }
        if(seasonIndex != -1) infoSpecificView.spinner_season.setSelection(seasonIndex)
    }

    companion object {
        // Extras that can be passed to this fragment
        val EXTRA_CONTENT_ID = "CONTENT_ID"
        val EXTRA_CONTENT_TYPE = "CONTENT_TYPE"
        // This extra represents the season to be selected at first
        val EXTRA_SEASON_INDEX = "SEASON_INDEX"
    }
}
