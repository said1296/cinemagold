package app.cinemagold.ui.browse.common.listener

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.ui.browse.common.recycleradapter.BaseInfiniteAutoScrollRVA

class ProgressOnScrollListener<T>(private val progressIndicators : MutableList<View>, private val layoutManager: LinearLayoutManager, val adapter: BaseInfiniteAutoScrollRVA<T>) : RecyclerView.OnScrollListener(){
    private val baseProgressListener = BaseProgressListener(progressIndicators)

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        baseProgressListener.handleDisplayProgress()
        val lastVisibleItemPosition = adapter.getRealPosition(layoutManager.findLastVisibleItemPosition())
        baseProgressListener.progressHandler(lastVisibleItemPosition+1, adapter.realItemCount)
        super.onScrolled(recyclerView, dx, dy)
    }
}
