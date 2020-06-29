package app.cinemagold.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentGroupedByGenre
import app.cinemagold.model.content.ContentType
import app.cinemagold.ui.MainActivity
import app.cinemagold.ui.common.recycleradapter.ContentHorizontalRVA
import app.cinemagold.ui.common.recycleradapter.ContentVerticalRVA
import app.cinemagold.ui.preview.PreviewFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.widget_content_grouped_by_genre.view.*
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
    lateinit var contentHorizontalRVAProvider : Provider<ContentHorizontalRVA>
    lateinit var homeFragmentLinearLayout: LinearLayout
    lateinit var contentGroupedByGenres : List<ContentGroupedByGenre>
    var recyclerViews : MutableList<RecyclerView> = mutableListOf()

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Observers
        val observerError = Observer<String>{
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
        viewModel.error.observe(this, observerError)
        val observerContentGroupedByGenre = Observer<List<ContentGroupedByGenre>>{ data ->
            contentGroupedByGenres = data
            inflateContentGroupedByGenres()
        }
        viewModel.contentGroupedByGenre.observe(this, observerContentGroupedByGenre)
        val observerContentPremiere = Observer<List<Content>>{ data ->
            contentVerticalRVA.setDataset(data)
        }
        viewModel.contentPremiere.observe(this, observerContentPremiere)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        homeFragmentLinearLayout = view.home

        //Recycler views
        recyclerViews.add(view.content_recycler_content_premiere)
        view.content_recycler_content_premiere.apply {
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = contentVerticalRVA
        }
        contentVerticalRVA.clickHandler = { contentId, contentType ->
            navigateToPreview(contentId, contentType)
        }

        //Inflate genres generic_recycler view only if the observerContentGroupedByGenre has been called
        if(this::contentGroupedByGenres.isInitialized){
            inflateContentGroupedByGenres()
        }
        return view
    }

    override fun onStop() {
        homeFragmentLinearLayout.removeAllViews()

        System.gc()
        super.onStop()
    }

    private fun navigateToPreview(contentId : Int, contentType: Int){
        //Set contentId for Preview
        setFragmentResult("preview", bundleOf("contentId" to contentId, "contentType" to ContentType.from(contentType)))
        (this.activity as MainActivity).addOrReplaceFragment(PreviewFragment(), "PREVIEW")
    }

    //Inflate genres generic_recycler views
    private fun inflateContentGroupedByGenres(){
        for(item in contentGroupedByGenres){
            val contentOfGenreRVAInstance = contentHorizontalRVAProvider.get()
            //Set callback for navigation to Preview on click of an item
            contentOfGenreRVAInstance.clickHandler = {contentId, contentType ->
                navigateToPreview(contentId, contentType)
            }
            val view = layoutInflater.inflate(R.layout.widget_content_grouped_by_genre, null)
            recyclerViews.add(view.content_grouped_by_genre_recycler_content_of_genre)
            view.widget_content_grouped_by_genre_title.text = item.name
            view.content_grouped_by_genre_recycler_content_of_genre.apply {
                adapter = contentOfGenreRVAInstance
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            contentOfGenreRVAInstance.setDataset(item.contents)
            homeFragmentLinearLayout.addView(view)
        }
    }
}
