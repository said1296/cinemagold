package app.cinemagold.ui.common.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.ui.MainActivity
import app.cinemagold.ui.common.recycleradapter.ContentHorizontalRVA
import kotlinx.android.synthetic.main.widget_content_grouped_by_genre.view.*
import javax.inject.Inject
import javax.inject.Provider

class ContentGroupedByGenreFragment : Fragment() {
    @Inject
    lateinit var contentHorizontalRVAProvider : Provider<ContentHorizontalRVA>
    lateinit var contentGroupedByGenres : List<ContentGroupedByGenre>
    lateinit var containerView : LinearLayoutCompat

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.generic_linear_layout, container, false) as LinearLayoutCompat
        containerView = view
        inflateContentGroupedByGenres()
        return view
    }

    fun updateContents(contentsNew : List<ContentGroupedByGenre>){
        println(this.parentFragment.toString())
        contentGroupedByGenres = contentsNew
        if(this::contentHorizontalRVAProvider.isInitialized && view!=null){
            inflateContentGroupedByGenres()
        }
    }

    //Inflate genres generic_recycler views
    private fun inflateContentGroupedByGenres(){
        println(contentGroupedByGenres)
        containerView.removeAllViews()
        System.gc()
        for(item in contentGroupedByGenres){
            val contentOfGenreRVAInstance = contentHorizontalRVAProvider.get()
            //Set callback for navigation to Preview on click of an item
            contentOfGenreRVAInstance.clickHandler = {contentId, contentType ->
                (activity as MainActivity).navigateToPreview(contentId, contentType)
            }
            val contentGroupedByGenreView = layoutInflater.inflate(R.layout.widget_content_grouped_by_genre, null)
            contentGroupedByGenreView.widget_content_grouped_by_genre_title.text = item.name
            contentGroupedByGenreView.content_grouped_by_genre_recycler_content_of_genre.apply {
                adapter = contentOfGenreRVAInstance
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            contentOfGenreRVAInstance.setDataset(item.contents)
            containerView.addView(contentGroupedByGenreView)
        }
    }
}