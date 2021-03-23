package app.cinemagold.ui.browse.common.fragment

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.ui.browse.common.recycleradapter.BaseInfiniteAutoScrollRVA

open class AutoScrollFragment: Fragment() {
    val recyclerViewsToScroll: MutableList<RecyclerView> = mutableListOf()
    var focusedRecyclerViewId: Int = -1
    val scrollIntervalMs: Long = 4000
    private val handler = Handler(Looper.getMainLooper())
    private val scrollRecyclerViewsRunnable = Runnable { autoScrollRecyclerViews() }
    var isTelevision: Boolean = false

    private fun autoScrollRecyclerViews(){
        for(recyclerView in recyclerViewsToScroll)
            if(focusedRecyclerViewId != recyclerView.id)
                recyclerView.smoothScrollBy((recyclerView.adapter as BaseInfiniteAutoScrollRVA<*>).itemWidth, 0, null, 2000)
        handler.removeCallbacks(scrollRecyclerViewsRunnable)
        handler.postDelayed(scrollRecyclerViewsRunnable, scrollIntervalMs)
    }

    fun startScrolling(){
        if(isTelevision){
            handler.postDelayed(scrollRecyclerViewsRunnable, scrollIntervalMs)
        }
    }

    fun stopScrolling() {
        handler.removeCallbacks(scrollRecyclerViewsRunnable)
    }

    fun changedScrollingRecyclerFocus(hasFocus: Boolean, id: Int){
        focusedRecyclerViewId = if(hasFocus) id  else -1
    }
}
