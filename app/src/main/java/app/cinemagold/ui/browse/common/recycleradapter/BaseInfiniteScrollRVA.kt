package app.cinemagold.ui.browse.common.recycleradapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


abstract class BaseInfiniteScrollRVA<T>: RecyclerView.Adapter<BaseInfiniteScrollRVA.ViewHolder>() {
    var dataset: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    val realItemCount: Int
        get() = dataset.size
    var layout: Int = 0
    val startPosition: Int
        get() = (Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2) % realItemCount

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        // This enables infinite scrolling but messes up position and itemCount which have to be calculated
        return if(dataset.isEmpty()) 0 else Integer.MAX_VALUE
    }

    override fun getItemViewType(position: Int) = 1

    // Calculate real position
    fun getRealPosition(position: Int): Int {
        if(position == 0) return 0
        return position % dataset.size
    }
}
