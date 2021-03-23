package app.cinemagold.ui.browse.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.fragment.AutoScrollFragment
import app.cinemagold.ui.browse.common.listener.ProgressOnScrollListener
import app.cinemagold.ui.browse.common.recycleradapter.ContentHorizontalRVA
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.navbar.view.*
import kotlinx.android.synthetic.main.widget_progress_indicators.view.*
import javax.inject.Inject
import javax.inject.Provider

class HomeFragment : AutoScrollFragment() {
    @Inject
    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var picasso : Picasso
    @Inject
    lateinit var contentVerticalRVA: ContentVerticalRVA
    @Inject
    lateinit var contentRecentRVA: ContentRecentRVA
    @Inject
    lateinit var contentHorizontalRVAProvider : Provider<ContentHorizontalRVA>
    var recyclerViews : MutableList<RecyclerView> = mutableListOf()

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isTelevision = resources.getBoolean(R.bool.isTelevision)

        //Observers
        viewModel.error.observe(this){data->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentGroupedByGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGroupedByGenre(data)
        }
        viewModel.contentPremiere.observe(this){data ->
            contentVerticalRVA.dataset = data
        }
        viewModel.contentRecent.observe(this){data ->
            contentRecentRVA.dataset = data
            if(data.size==0){
                requireView().content_recent_title.visibility = View.GONE
                requireView().content_recycler_content_recent.visibility = View.GONE
                return@observe
            }
            buildRecent()
        }
        viewModel.contentRecentSelected.observe(this){data ->
            if(viewModel.recentSelected.mediaType.id== ContentType.MOVIE.value){
                (activity as BrowseActivity).navigateToPlayer(data, viewModel.recentSelected.elapsed)
            }else{
                (activity as BrowseActivity).navigateToPlayer(data, viewModel.seasonSelectedIndex, viewModel.episodeSelectedIndex, viewModel.recentSelected.elapsed)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        //Handle views for TV
        if((activity as BrowseActivity).isTelevision)
            rootView.home_header.visibility = View.GONE

        //Hide recent until it has values
        rootView.content_recent_title.visibility = View.GONE
        rootView.content_recycler_content_recent.visibility = View.GONE

        //Recycler views
        val progressIndicators : MutableList<View> = mutableListOf()
        for(i in 0 until rootView.content_premiere_header.progress_indicators.childCount){
            progressIndicators.add(rootView.content_premiere_header.progress_indicators.getChildAt(i))
        }
        rootView.content_recycler_content_premiere.apply {
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = contentVerticalRVA
            if(!(activity as BrowseActivity).isTelevision)
                addOnScrollListener(ProgressOnScrollListener(progressIndicators, layoutManager as LinearLayoutManager, adapter as ContentVerticalRVA))
            recyclerViewsToScroll.add(this)
        }
        contentVerticalRVA.apply {
            clickHandler = { contentId, contentType ->
                (activity as BrowseActivity).navigateToPreview(contentId, contentType)
            }
            changedFocus = { hasFocus ->
                this@HomeFragment.changedScrollingRecyclerFocus(hasFocus, rootView.content_recycler_content_premiere.id)
            }
            this.progressIndicators = progressIndicators
        }

        startScrolling()

        if(isTelevision) requireActivity().findViewById<AppCompatTextView>(R.id.navbar_home).requestFocus()

        return rootView
    }

    override fun onStart() {
        super.onStart()
        viewModel.startedFragment()
    }

    private fun buildRecent(){
        val progressIndicators : MutableList<View> = mutableListOf()
        for(i in 0 until view!!.content_recent_header.progress_indicators.childCount){
            progressIndicators.add(view!!.content_recent_header.progress_indicators.getChildAt(i))
        }
        view!!.content_recent_title.visibility = View.VISIBLE
        view!!.content_recycler_content_recent.visibility = View.VISIBLE
        view!!.content_recycler_content_recent.apply {
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = contentRecentRVA
            if(!(activity as BrowseActivity).isTelevision)
                addOnScrollListener(ProgressOnScrollListener(progressIndicators, layoutManager as LinearLayoutManager, adapter as ContentRecentRVA))
        }
        contentRecentRVA.apply {
            clickHandler = {data ->
                viewModel.clickedRecent(data)
            }
            changedFocus = { hasFocus ->
                this@HomeFragment.changedScrollingRecyclerFocus(hasFocus, view!!.content_recycler_content_recent.id)
            }
            this.progressIndicators = progressIndicators
        }
    }
}
