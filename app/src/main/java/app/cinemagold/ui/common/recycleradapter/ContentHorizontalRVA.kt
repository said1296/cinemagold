package app.cinemagold.ui.common.recycleradapter;

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.content.Content
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_content_horizontal.view.*
import javax.inject.Inject


class ContentHorizontalRVA @Inject constructor(
    private var dataset : List<Content>,
    val context : Context,
    private val picasso : Picasso
) : RecyclerView.Adapter<ContentHorizontalRVA.ViewHolder>() {
    lateinit var sliderView : ImageView
    //Elevation value adds the same amount as padding so it's necessary to compensate
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal) -
            context.resources.getDimensionPixelSize(R.dimen.item_content_horizontal_elevation)
    private val scale : Int = 30
    lateinit var clickHandler : (Int, Int) -> Unit

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_horizontal, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = dataset[position]
        val item = holder.itemView
        item.item_content_horizontal_title.text = currentData.name
        item.item_content_horizontal_metadata.text =
            "${currentData.length} · ${currentData.genreMain.name} · ${currentData.genreSecondary.name}"

        //Set background
        sliderView = item.item_content_horizontal_background
        picasso.load(currentData.sliderSrc)
            .config(Bitmap.Config.RGB_565)
            .resize(16*scale, 9*scale)
            .centerCrop().into(item.item_content_horizontal_background)
        //Set start and end padding
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if(position == 0){
            params.leftMargin = sideMargin
        } else if (position == dataset.lastIndex){
            params.rightMargin = sideMargin
        }

        item.setOnClickListener { clickHandler(currentData.id, currentData.mediaType.id) }
        holder.itemView.layoutParams = params
    }

    override fun getItemId(position: Int) = dataset[position].id.toLong()
    override fun getItemViewType(position: Int) = 1

    fun setDataset(datasetNew : List<Content>){
        dataset = datasetNew
        notifyDataSetChanged()
    }

}
