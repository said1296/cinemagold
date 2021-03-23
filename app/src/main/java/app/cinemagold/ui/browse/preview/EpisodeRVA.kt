package app.cinemagold.ui.browse.preview;

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.content.Episode
import app.cinemagold.ui.common.ContentItemTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_content_recent.view.*
import kotlinx.android.synthetic.main.item_episode.view.*
import javax.inject.Inject


class EpisodeRVA @Inject constructor(
    private var dataset : List<Episode>,
    val context : Context,
    private val picasso : Picasso
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //Elevation value adds the same amount as padding so it's necessary to compensate
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal) -
            context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_elevation)
    private val scale : Int = 30
    lateinit var clickHandler : (Int) -> Unit
    val isTelevision = context.resources.getBoolean(R.bool.isTelevision)
    var isPlayerActivity: Boolean = false

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentData = dataset[position]
        val item = holder.itemView

        // Set elapsed
        if(currentData.elapsedPercent == 0F || !isPlayerActivity) {
            item.item_episode_elapsed.visibility = View.GONE
            item.item_episode_remaining.visibility = View.GONE
        }
        else {
            item.item_episode_elapsed.apply {
                visibility = View.VISIBLE
                (layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth = currentData.elapsedPercent
            }
            item.item_episode_remaining.visibility = View.VISIBLE
        }

        item.item_episode_description.text = currentData.description
        item.item_episode_title.text = currentData.name
        item.item_episode_length.text = currentData.length
        item.item_episode_number.text = currentData.number
        item.id = dataset[position].id

        //Set background
        val target = ContentItemTarget(context.resources) { stateListDrawable ->
            item.item_episode_background.setImageDrawable(stateListDrawable)
        }
        //Keep strong reference to target with a tag to avoid garbage collection
        item.item_episode_background.tag = target
        picasso.load(currentData.thumbnailSrc)
            .config(Bitmap.Config.RGB_565)
            .resize(16*scale, 9*scale)
            .centerCrop().into(target)

        //Set start and end padding for mobile, and focuses for TV
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if(position == 0){
            if(!isTelevision) params.leftMargin = sideMargin
            else{
                item.nextFocusUpId = R.id.spinner_season
                item.nextFocusDownId = dataset[position+1].id
            }
        } else if (position == dataset.lastIndex){
            if(!isTelevision) params.rightMargin = sideMargin
            else{
                item.nextFocusUpId = dataset[position-1].id
                item.nextFocusDownId = item.id
            }
        } else {
            if(isTelevision){
                item.nextFocusUpId = dataset[position-1].id
                item.nextFocusDownId = dataset[position+1].id
            }
        }
        if(isTelevision){
            item.nextFocusLeftId = item.id
            item.nextFocusRightId = item.id
        }
        holder.itemView.layoutParams = params

        item.setOnClickListener { clickHandler(position) }
    }

    override fun getItemId(position: Int) = dataset[position].id.toLong()
    override fun getItemViewType(position: Int) = 1

    fun setDataset(datasetNew : List<Episode>){
        dataset = datasetNew
        notifyDataSetChanged()
    }

}
