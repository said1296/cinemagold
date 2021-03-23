package app.cinemagold.ui.browse.common.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.listener.ProgressOnScrollListener
import app.cinemagold.ui.browse.common.recycleradapter.ContentHorizontalRVA
import app.cinemagold.ui.browse.home.HomeFragment
import kotlinx.android.synthetic.main.widget_content_grouped_by_genre.view.*
import kotlinx.android.synthetic.main.widget_progress_indicators.view.*
import javax.inject.Inject
import javax.inject.Provider

class ContentGroupedByGenreFragment : AutoScrollFragment() {
    @Inject
    lateinit var contentHorizontalRVAProvider : Provider<ContentHorizontalRVA>
    lateinit var contentGroupedByGenres : List<ContentGroupedByGenre>
    lateinit var containerView : LinearLayoutCompat

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTelevision = resources.getBoolean(R.bool.isTelevision)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.generic_linear_layout, container, false) as LinearLayoutCompat
        containerView = view
        //Compensate bottom padding for navbar
        view.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.bottom_navbar_height) + 10)

        if(this::contentGroupedByGenres.isInitialized)
            inflateContentGroupedByGenres()

        if((activity as BrowseActivity).currentFragmentTag == HomeFragment::class.simpleName) startScrolling()
        else stopScrolling()

        return view
    }

    fun updateContents(contentsNew : List<ContentGroupedByGenre>){
        contentGroupedByGenres = contentsNew
        if(this::contentHorizontalRVAProvider.isInitialized && view!=null){
            inflateContentGroupedByGenres()
        }
    }

    //Inflate genres generic_recycler views
    private fun inflateContentGroupedByGenres(){
        containerView.removeAllViews()
        recyclerViewsToScroll.clear()
        System.gc()
        for((index, item) in contentGroupedByGenres.withIndex()){
            val contentGroupedByGenreView = layoutInflater.inflate(R.layout.widget_content_grouped_by_genre, null)
            val progressIndicators : MutableList<View> = mutableListOf()
            for(i in 0 until contentGroupedByGenreView.progress_indicators.childCount){
                progressIndicators.add(contentGroupedByGenreView.progress_indicators.getChildAt(i))
            }
            val contentOfGenreRVAInstance = contentHorizontalRVAProvider.get()
            //Set callback for navigation to Preview on click of an item and prefix for id to keep track of navigation
            contentOfGenreRVAInstance.apply {
                recyclerView = contentGroupedByGenreView.content_grouped_by_genre_recycler_content_of_genre
                clickHandler = {contentId, contentType ->
                    (activity as BrowseActivity).navigateToPreview(contentId, contentType)
                }
                this.progressIndicators = progressIndicators
                idPrefix = (index+1)*1000
                dataset = item.contents
                changedFocus = { hasFocus ->
                    this@ContentGroupedByGenreFragment.changedScrollingRecyclerFocus(hasFocus, index)
                }
            }
            contentGroupedByGenreView.widget_content_grouped_by_genre_title.text = item.name
            contentGroupedByGenreView.content_grouped_by_genre_recycler_content_of_genre.apply {
                id = index
                if(!(activity as BrowseActivity).isTelevision)
                    addOnScrollListener(ProgressOnScrollListener(progressIndicators, layoutManager as LinearLayoutManager, adapter as ContentHorizontalRVA))
                recyclerViewsToScroll.add(this)
            }
            containerView.addView(contentGroupedByGenreView)
        }
    }
}
