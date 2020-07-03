package app.cinemagold.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.MainActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_preview.view.*
import kotlinx.android.synthetic.main.preview_info_specific_movie.view.*
import kotlinx.android.synthetic.main.preview_info_specific_serialized.view.*
import kotlinx.android.synthetic.main.widget_button_play.view.*
import java.util.stream.Collectors
import javax.inject.Inject

class PreviewFragment : Fragment() {
    @Inject
    lateinit var viewModel: PreviewViewModel
    @Inject
    lateinit var picasso : Picasso
    @Inject
    lateinit var episodeRVA : EpisodeRVA
    var contentId : Int = -1
    lateinit var contentType : ContentType
    lateinit var content : Content
    private val scale : Int = 30
    private var currentSeasonIndex : Int = 0
    //Views
    lateinit var infoSpecificContainerView : LinearLayout
    lateinit var titleView : TextView
    lateinit var genreMainView : TextView
    lateinit var genreSecondaryView : TextView
    lateinit var descriptionShortView: TextView
    lateinit var sliderView : ImageView
    lateinit var scoreView : RatingBar
    lateinit var infoSpecificView : View

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Listen for contentId set on Home fragment
        setFragmentResultListener("preview") { _, bundle ->
            contentId = bundle.getInt("contentId")
            contentType = bundle.get("contentType") as ContentType
            viewModel.receivedContentIdAndContentType(contentId, contentType)
        }

        //Observers
        viewModel.error.observe(this) {data ->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.content.observe(this) {data ->
            val contentSource : String
            content = data
            updateCommonView()
            if(viewModel.currentContentType == ContentType.MOVIE){
                updateInfoSpecificMovieView()
                contentSource = content.movie.src
            } else {
                updateInfoSpecificSerializedView()
                contentSource = content.seasons[0].episodes[0].src
            }
            infoSpecificView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            infoSpecificContainerView.addView(infoSpecificView)

            //Set onClickListener for Play button that is added in any of the two specific views
            requireView().widget_button_play.setOnClickListener {
                (activity as MainActivity).navigateToPlayer(contentId, contentType, contentSource)
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

        return view
    }

    private fun updateCommonView(){
        titleView.text = content.name
        genreMainView.text = content.genreMain.name
        genreSecondaryView.text = content.genreSecondary.name
        descriptionShortView.text = content.descriptionShort
        scoreView.rating = content.score
    }
    //Add information specific to movies
    private fun updateInfoSpecificMovieView(){
        picasso.load(content.sliderSrc)
            .placeholder(R.drawable.bg_dark)
            .config(Bitmap.Config.RGB_565)
            .resize(16*scale, 9*scale)
            .centerCrop().into(sliderView)
        infoSpecificView = layoutInflater.inflate(R.layout.preview_info_specific_movie, null)
        infoSpecificView.preview_info_length.text = content.length
        infoSpecificView.preview_info_director.text = content.movie.director
    }

    //Add information specific to serialized content
    private fun updateInfoSpecificSerializedView(){
        picasso.load(content.seasons[currentSeasonIndex].sliderSrc)
            .placeholder(R.drawable.bg_dark)
            .config(Bitmap.Config.RGB_565)
            .resize(16*scale, 9*scale)
            .centerCrop().into(sliderView)

        infoSpecificView = layoutInflater.inflate(R.layout.preview_info_specific_serialized, null)

        //Episodes generic_recycler view
        infoSpecificView.preview_recycler_episode.apply {
            adapter = episodeRVA
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        episodeRVA.setDataset(content.seasons[currentSeasonIndex].episodes)

        //Season selector
        val seasonSpinnerItems = content.seasons.stream().map{ item -> "Temporada " + item.number.toString() }.collect(Collectors.toList())
        val adapter = ArrayAdapter<String>(context!!.applicationContext, R.layout.spinner_season, seasonSpinnerItems)
        adapter.setDropDownViewResource(R.layout.spinner_season_item)
        infoSpecificView.preview_spinner_season.adapter = adapter
        infoSpecificView.preview_spinner_season.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(currentSeasonIndex!=p2){
                    currentSeasonIndex = p2
                    episodeRVA.setDataset(content.seasons[currentSeasonIndex].episodes)
                    picasso.load(content.seasons[currentSeasonIndex].sliderSrc)
                        .placeholder(R.drawable.bg_dark)
                        .config(Bitmap.Config.RGB_565)
                        .resize(16*scale, 9*scale)
                        .centerCrop().into(sliderView)
                }
            }
        }
    }
}
