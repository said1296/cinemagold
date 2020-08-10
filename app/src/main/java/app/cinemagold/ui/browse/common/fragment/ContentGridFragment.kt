package app.cinemagold.ui.browse.common.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.common.recycleradapter.ContentGridRVA
import javax.inject.Inject
import kotlin.math.roundToInt

class ContentGridFragment : Fragment() {
    @Inject
    lateinit var contentGridRVA: ContentGridRVA
    lateinit var contents : List<Content>
    lateinit var activityFragmentManager : FragmentManager

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFragmentManager = (this.activity as BrowseActivity).supportFragmentManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.generic_recycler, container, false) as RecyclerView

        val padding = resources.getDimensionPixelSize(R.dimen.bottom_navbar_height)

        //Set callback for navigation to Preview on click of an item
        contentGridRVA.clickHandler = {contentId, contentType ->
            (this.activity as BrowseActivity).navigateToPreview(contentId, contentType)
        }

        val screenWidth = (this.activity as BrowseActivity).windowManager.defaultDisplay.width
        val numberOfColumns =
            (screenWidth / resources.getDimensionPixelSize(R.dimen.item_content_grid_width)).toDouble().roundToInt()

        view.apply {
            adapter = contentGridRVA
            layoutManager = GridLayoutManager(context, numberOfColumns)
            //Compensate bottom padding for navbar
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.bottom_navbar_height) + 10)
        }

        view.isNestedScrollingEnabled = false

        contentGridRVA.setDataset(contents)

        return view
    }

    fun updateContents(contentsNew : List<Content>){
        contents = contentsNew
        if(this::contentGridRVA.isInitialized && view!=null){
            contentGridRVA.setDataset(contents)
        }
    }
}