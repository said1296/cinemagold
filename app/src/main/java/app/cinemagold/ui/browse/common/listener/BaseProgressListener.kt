package app.cinemagold.ui.browse.common.listener

import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.math.ceil

class BaseProgressListener(private val progressIndicators : MutableList<View>) {
    private var isProgressShowing = false
    private val hideDelayMs = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private val hideProgressRunnable = Runnable { hideProgress() }

    fun handleDisplayProgress(){
        removeHideRunnablesScheduled()
        if(!isProgressShowing){
            isProgressShowing = true
            for(progressIndicator in progressIndicators){
                progressIndicator.visibility = View.VISIBLE
            }
        }
        handler.postDelayed(hideProgressRunnable, hideDelayMs)
    }

    fun removeHideRunnablesScheduled(){
        hideProgress()
        handler.removeCallbacks(hideProgressRunnable)
    }

    private fun hideProgress(){
        isProgressShowing = false
        for(progressIndicator in progressIndicators){
            progressIndicator.visibility = View.GONE
        }
    }

    fun progressHandler(position : Int, itemCount: Int){
        val progress =
            if(itemCount<4){
                if(position<2 && itemCount>1){
                    1
                } else {
                    progressIndicators.size
                }
            }else{
                val progressChunkSize = itemCount/ progressIndicators.size.toDouble()
                ceil(position / progressChunkSize).toInt()
            }
        for((progressIndicatorIndex, progressIndicator) in progressIndicators.withIndex()){
            progressIndicator.isSelected = progressIndicatorIndex == (progress - 1)
        }
    }
}
