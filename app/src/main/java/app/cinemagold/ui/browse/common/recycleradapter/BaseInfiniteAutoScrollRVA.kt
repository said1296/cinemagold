package app.cinemagold.ui.browse.common.recycleradapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class BaseInfiniteAutoScrollRVA<T>(val layout: Int, val itemWidth: Int) : RecyclerView.Adapter<BaseInfiniteAutoScrollRVA.ViewHolder>() {
    var focusChildPosition: Int = -1
    var focusing = false
    var recyclerView: RecyclerView? = null
    set(value) {
        value!!.apply {
            adapter = this@BaseInfiniteAutoScrollRVA
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            (layoutManager as LinearLayoutManager).scrollToPosition(dataset.size * 40 + 1)
        }
        field = value
    }
    val realItemCount: Int
        get() {
            return dataset.size
        }

    var dataset: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var currentPosition = -1
    lateinit var changedFocus: (Boolean) -> Unit

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun getRealPosition(position: Int): Int {
        return position % realItemCount
    }
    override fun getItemCount(): Int {
        return if (dataset.isEmpty()) 0 else Int.MAX_VALUE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int) = 1
}
