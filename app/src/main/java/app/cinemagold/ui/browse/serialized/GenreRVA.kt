package app.cinemagold.ui.browse.serialized;

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.generic.GenericIdAndName
import kotlinx.android.synthetic.main.item_genre.view.*
import javax.inject.Inject


class GenreRVA @Inject constructor(
    private var dataset : List<GenericIdAndName>,
    val context : Context
) : RecyclerView.Adapter<GenreRVA.ViewHolder>() {
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal)
    lateinit var clickHandler : (GenericIdAndName) -> Unit
    var selectedPosition : Int = 0
    private var lightColor : Int
    private var lightDarkColor : Int

    init {
        setHasStableIds(true)
        //Get colors from theme
        val typedValue = TypedValue()
        val theme : Resources.Theme = ContextThemeWrapper(context, R.style.AppTheme).theme
        theme.resolveAttribute(R.attr.light, typedValue, true)
        lightColor = context.getColor(typedValue.resourceId)
        theme.resolveAttribute(R.attr.lightDark, typedValue, true)
        lightDarkColor = context.getColor(typedValue.resourceId)
    }


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = dataset[position]
        val item = holder.itemView as TextView
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        item.item_genre.text = currentData.name

        //Highlight selected
        if(selectedPosition==position){
            item.setTextColor(lightColor)
        }else{
            item.setTextColor(lightDarkColor)
        }

        //Show or hide "All genres" button
        if(position == 0 && selectedPosition==0){
            item.visibility = View.GONE
            params.width = 0
            params.rightMargin = 0
        }

        //Set start and end padding
        if(position == 0){
            params.leftMargin = sideMargin
        } else if (position == dataset.lastIndex){
            params.rightMargin = sideMargin
        }

        item.setOnClickListener {
            val selectedPositionOld = selectedPosition
            selectedPosition = position
            notifyItemChanged(selectedPositionOld)
            notifyItemChanged(selectedPosition)
            clickHandler(currentData)
        }
    }

    override fun getItemId(position: Int) = dataset[position].id.toLong()
    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setDataset(datasetNew : List<GenericIdAndName>){
        dataset = datasetNew
        notifyDataSetChanged()
    }

}
