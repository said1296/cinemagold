package app.cinemagold.ui.option.profilecreate

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.ui.common.ContentItemTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_avatar_grid.view.*
import javax.inject.Inject


class AvatarGridRVA @Inject constructor(
    private var dataset : List<IdAndName>,
    val context : Context,
    private val picasso : Picasso
) : RecyclerView.Adapter<AvatarGridRVA.ViewHolder>() {
    private val itemMargin = context.resources.getDimensionPixelSize(R.dimen.item_avatar_grid_margin)
    lateinit var clickHandler : (IdAndName) -> Unit

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avatar_grid, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = dataset[position]
        val item = holder.itemView
        item.id = currentData.id

        val params = item.layoutParams as RecyclerView.LayoutParams
        params.rightMargin = itemMargin
        params.leftMargin = itemMargin
        params.topMargin = itemMargin
        params.bottomMargin = itemMargin

        //Set background
        val target = ContentItemTarget(context.resources) { stateListDrawable ->
            item.item_avatar_grid_background.setImageDrawable(stateListDrawable)
        }
        //Keep strong reference to target with a tag to avoid garbage collection
        item.item_avatar_grid_background.tag = target
        picasso.load(currentData.name)
            .config(Bitmap.Config.RGB_565)
            .into(target)

        item.setOnClickListener { clickHandler(currentData) }
    }

    override fun getItemId(position: Int) = dataset[position].id.toLong()
    override fun getItemViewType(position: Int) = 1

    fun setDataset(datasetNew : List<IdAndName>){
        dataset = datasetNew
        notifyDataSetChanged()
    }

}
