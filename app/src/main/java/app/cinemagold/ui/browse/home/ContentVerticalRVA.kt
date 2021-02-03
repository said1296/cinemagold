package app.cinemagold.ui.browse.home;

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.content.Content
import app.cinemagold.ui.browse.common.listener.ProgressOnFocusListener
import app.cinemagold.ui.browse.common.recycleradapter.BaseInfiniteScrollRVA
import app.cinemagold.ui.common.ContentItemTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_content_vertical.view.*


class ContentVerticalRVA (
    val context : Context,
    private val picasso : Picasso
) : BaseInfiniteScrollRVA<Content>() {
    //Elevation value adds the same amount as padding so it's necessary to compensate
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal) -
            context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_elevation)
    private val scale : Int = 120
    lateinit var clickHandler : (Int, Int) -> Unit
    lateinit var progressIndicators : MutableList<View>
    val isTelevision = context.resources.getBoolean(R.bool.isTelevision)

    init {
        layout = R.layout.item_content_vertical
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val position = getRealPosition(position)
        val currentData = dataset[position]
        val item = holder.itemView
        item.id = dataset[position].id
        item.background
        item.item_content_vertical_title.text = currentData.name

        //Set background
        val target = ContentItemTarget(context.resources)
        picasso.load(currentData.posterSrc)
            .config(Bitmap.Config.RGB_565)
            .resize(2*scale, 3*scale)
            .centerCrop().into(target)
        //Add tag to keep strong reference to target and avoid Garbage Collection
        item.item_content_vertical_background.tag = target
        item.item_content_vertical_background.setImageDrawable(target.stateListDrawable)


        item.item_content_vertical_new_season.visibility =
            if(currentData.hasNewSeason) View.VISIBLE
            else View.GONE

        //Set start and end padding
        val params = item.layoutParams as RecyclerView.LayoutParams
        if(position == 0){
            params.leftMargin = sideMargin
            item.nextFocusLeftId = dataset[realItemCount-1].id
            item.nextFocusRightId = dataset[position+1].id
        } else if (position == dataset.lastIndex){
            params.rightMargin = sideMargin
            item.nextFocusLeftId = dataset[position-1].id
            item.nextFocusRightId = dataset[0].id
        } else {
            item.nextFocusLeftId = dataset[position-1].id
            item.nextFocusRightId = dataset[position+1].id
        }
        item.setOnClickListener { clickHandler(currentData.id, currentData.mediaType.id) }
        if(isTelevision)
            item.onFocusChangeListener = ProgressOnFocusListener(progressIndicators, position+1, realItemCount)
    }
}
