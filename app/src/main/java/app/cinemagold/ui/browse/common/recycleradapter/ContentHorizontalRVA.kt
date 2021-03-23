package app.cinemagold.ui.browse.common.recycleradapter;

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.content.Content
import app.cinemagold.ui.browse.common.listener.ProgressOnFocusListener
import app.cinemagold.ui.common.ContentItemTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_content_horizontal.view.*
import javax.inject.Inject


class ContentHorizontalRVA @Inject constructor(
    val context: Context,
    private val picasso: Picasso
) : BaseInfiniteAutoScrollRVA<Content>(
    R.layout.item_content_horizontal,
    context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_width) + context.resources.getDimensionPixelSize(
        R.dimen.item_content_margin_right
    )
) {
    lateinit var sliderView: ImageView
    private val scale: Int = 30
    lateinit var clickHandler: (Int, Int) -> Unit
    lateinit var progressIndicators: MutableList<View>
    var idPrefix: Int = -1
    val isTelevision = context.resources.getBoolean(R.bool.isTelevision)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val position = getRealPosition(position)
        val currentData = dataset[position]
        val item = holder.itemView
        item.id = idPrefix + position
        item.item_content_horizontal_title.text = currentData.name
        item.item_content_horizontal_metadata.text =
            "${currentData.length} · ${currentData.genreMain.name} · ${currentData.genreSecondary.name}"

        //Set background
        sliderView = item.item_content_horizontal_background
        val target = ContentItemTarget(context.resources)

        picasso.load(currentData.sliderSrc)
            .config(Bitmap.Config.RGB_565)
            .resize(16 * scale, 9 * scale)
            .centerCrop().into(target)
        //Add tag to keep strong reference to target and avoid Garbage Collection
        sliderView.tag = target
        sliderView.setImageDrawable(target.stateListDrawable)

        item.item_content_horizontal_new_season.visibility =
            if (currentData.hasNewSeason) View.VISIBLE
            else View.GONE

        //Set start and end padding
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (position == 0) {
            item.nextFocusLeftId = item.id + 3
            item.nextFocusRightId = item.id + 1
        } else if (position == dataset.lastIndex) {
            item.nextFocusLeftId = item.id - 1
            item.nextFocusRightId = idPrefix
        } else {
            item.nextFocusLeftId = item.id - 1
            item.nextFocusRightId = item.id + 1
        }


        item.setOnClickListener { clickHandler(currentData.id, currentData.mediaType.id) }
        if (isTelevision)
            item.onFocusChangeListener =
                ProgressOnFocusListener(progressIndicators, position + 1, realItemCount) { hasFocus ->

                    changedFocus(hasFocus)
                }
        else
            item.setOnFocusChangeListener { _, hasFocus -> changedFocus(hasFocus) }
        holder.itemView.layoutParams = params
    }

}
