package app.cinemagold.ui.browse.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.recycleradapter.ContentHorizontalRVA
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import javax.inject.Inject
import javax.inject.Provider

class HomeFragment : Fragment() {
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

        //Observers
        viewModel.error.observe(this){data->
            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }
        viewModel.contentGroupedByGenre.observe(this){data ->
            (activity as BrowseActivity).changeContentGroupedByGenre(data)
        }
        viewModel.contentPremiere.observe(this){data ->
            contentVerticalRVA.setDataset(data)
        }
        viewModel.contentRecent.observe(this){data ->
            contentRecentRVA.setDataset(data)
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
        rootView.content_recycler_content_premiere.apply {
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = contentVerticalRVA
        }
        contentVerticalRVA.clickHandler = { contentId, contentType ->
            (activity as BrowseActivity).navigateToPreview(contentId, contentType)
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        viewModel.startedFragment()
    }

    private fun buildRecent(){
        view!!.content_recent_title.visibility = View.VISIBLE
        view!!.content_recycler_content_recent.visibility = View.VISIBLE
        view!!.content_recycler_content_recent.apply {
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = contentRecentRVA
        }
        contentRecentRVA.clickHandler = {data ->
            viewModel.clickedRecent(data)
        }
    }
}
