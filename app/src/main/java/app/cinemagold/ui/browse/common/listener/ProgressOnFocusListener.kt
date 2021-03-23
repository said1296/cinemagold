package app.cinemagold.ui.browse.common.listener

import android.view.View

class ProgressOnFocusListener(progressIndicators : MutableList<View>, private val position: Int, private val itemCount: Int, private val changedFocus: (Boolean) -> Unit) : View.OnFocusChangeListener {
    private val baseProgressListener = BaseProgressListener(progressIndicators)

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        changedFocus(hasFocus)
        if(hasFocus){
            baseProgressListener.handleDisplayProgress()
            baseProgressListener.progressHandler(position, itemCount)
        }else{
            baseProgressListener.removeHideRunnablesScheduled()
        }
    }
}
