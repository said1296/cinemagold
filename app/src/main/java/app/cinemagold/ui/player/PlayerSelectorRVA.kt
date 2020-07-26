package app.cinemagold.ui.player;

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.model.generic.GenericIdAndName
import javax.inject.Inject


class PlayerSelectorRVA @Inject constructor(
    private var dataset : List<GenericIdAndName>,
    val context : Context
) : RecyclerView.Adapter<PlayerSelectorRVA.ViewHolder>() {
    private val sideMargin = context.resources.getDimensionPixelSize(R.dimen.standard_margin_horizontal)
    lateinit var clickHandler : (GenericIdAndName) -> Unit
    var selectedId : Int = -1
    private var lightColor : Int
    private var lightDarkColor : Int
    private val verticalMargin = context.resources.getDimensionPixelSize(R.dimen.item_player_selector_margin_vertical)

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
            .inflate(R.layout.item_player_selector, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = dataset[position]
        val item = holder.itemView as TextView
        item.text = currentData.name

        //Highlight selected
        if(selectedId==getItemId(position).toInt()){
            item.setTextColor(lightColor)
        }else{
            item.setTextColor(lightDarkColor)
        }

        //Set params programmatically since XML values are ignored
        val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.topMargin = verticalMargin
        params.bottomMargin = verticalMargin
        item.layoutParams = params
        item.gravity = Gravity.CENTER

        item.setOnClickListener {
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
