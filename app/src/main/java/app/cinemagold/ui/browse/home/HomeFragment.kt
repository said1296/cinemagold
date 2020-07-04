package app.cinemagold.ui.browse.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.recycleradapter.ContentHorizontalRVA
import app.cinemagold.ui.browse.common.recycleradapter.ContentVerticalRVA
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
    lateinit var contentHorizontalRVAProvider : Provider<ContentHorizontalRVA>
    lateinit var homeFragmentLinearLayout: LinearLayout
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
            (activity as BrowseActivity).navigateToPreview(contentId, contentType)
        }
        
        return view
    }
}
