package app.cinemagold.ui.browse.home;

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.Recent
import app.cinemagold.ui.browse.common.listener.ProgressOnFocusListener
import app.cinemagold.ui.browse.common.recycleradapter.BaseInfiniteAutoScrollRVA
import app.cinemagold.ui.common.ContentItemTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_content_recent.view.*
import javax.inject.Inject


class ContentRecentRVA @Inject constructor(
    val context: Context,
    private val picasso: Picasso
) : BaseInfiniteAutoScrollRVA<Recent>(
    R.layout.item_content_recent,
    context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_width) + context.resources.getDimensionPixelSize(
        R.dimen.item_content_margin_right
    )
) {
    //Elevation value adds the same amount as padding so it's necessary to compensate
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal) -
            context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_elevation)
    private val scale: Int = 30
    lateinit var clickHandler: (Recent) -> Unit
    lateinit var progressIndicators: MutableList<View>
    val isTelevision = context.resources.getBoolean(R.bool.isTelevision)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val position = getRealPosition(position)
        val currentData = dataset[position]
        val item = holder.itemView
        item.id = position
        item.item_content_recent_title.text = currentData.name
        item.item_content_recent_metadata.text =
            "${currentData.length}m · ${currentData.genreMain.name} · ${currentData.genreSecondary.name}"

        val elapsedBarParams = item.item_content_recent_elapsed.layoutParams as ConstraintLayout.LayoutParams
        elapsedBarParams.matchConstraintPercentWidth = currentData.elapsedPercent

        item.item_content_recent_info.text =
            if (currentData.mediaType.id != ContentType.MOVIE.value) "T${currentData.seasonNumber} · Cap.${currentData.episode!!.number}"
            else ""

        //Set background
        val target = ContentItemTarget(context.resources)
        picasso.load(currentData.sliderSrc)
            .config(Bitmap.Config.RGB_565)
            .resize(16 * scale, 9 * scale)
            .centerCrop().into(target)
        //Add tag to keep strong reference to target and avoid Garbage Collection
        item.item_content_recent_background.tag = target
        item.item_content_recent_background.setImageDrawable(target.stateListDrawable)
        //Set start and end padding
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (position == 0) {
            item.nextFocusLeftId = realItemCount - 1
            item.nextFocusRightId = position + 1
        } else if (position == dataset.lastIndex) {
            item.nextFocusLeftId = position - 1
            item.nextFocusRightId = 0
        } else {
            item.nextFocusLeftId = position - 1
            item.nextFocusRightId = position + 1
        }

        item.setOnClickListener {
            clickHandler(currentData)
        }
        if (isTelevision)
            item.onFocusChangeListener = ProgressOnFocusListener(progressIndicators, position + 1, realItemCount) {
                changedFocus(it)
            }
        holder.itemView.layoutParams = params
    }
}
