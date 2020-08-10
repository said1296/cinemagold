package app.cinemagold.ui.option.profilecreate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.option.OptionActivity
import kotlinx.android.synthetic.main.fragment_avatar_grid.view.*
import javax.inject.Inject
import kotlin.math.roundToInt

class AvatarGridFragment : Fragment() {
    @Inject
    lateinit var viewModel : AvatarGridViewModel
    @Inject
    lateinit var avatarGridRVA : AvatarGridRVA

    override fun onCreate(savedInstanceState: Bundle?) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        //Observers
        viewModel.avatars.observe(this){data->
            avatarGridRVA.setDataset(data)
        }

        avatarGridRVA.clickHandler = {data ->
            (parentFragment as ProfileCreateFragment).selectedAvatar(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_avatar_grid, container, false)
        rootView.avatar_grid_recycler

        val screenWidth = (this.activity as OptionActivity).windowManager.defaultDisplay.width
        val numberOfColumns =
            (screenWidth / resources.getDimensionPixelSize(R.dimen.item_avatar_grid_width)).toDouble().roundToInt()
        rootView.avatar_grid_recycler.apply {
            adapter = avatarGridRVA
            layoutManager = GridLayoutManager(context, numberOfColumns)
        }

        return rootView
    }
}