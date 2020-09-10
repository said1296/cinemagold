package app.cinemagold.ui.player

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.Player


class PlayerListener(val onLoadedCallback : () -> Unit, val updateElapsed : () -> Unit, val onPlayChanged : (isOnPlay: Boolean) -> Unit) : Player.EventListener {
    private var isFirstTimePlaying = true
    private val updateElapsedDelay = 5000L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object: Runnable {
        override fun run() {
            updateElapsed()
            handler.postDelayed(this, updateElapsedDelay);
        }
    }
    private var isOnPlay = false

    private var isElapsedUpdating = false

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //Wait for data to be loaded before accessing players' mappedTrackInfo
        if(playbackState==Player.STATE_READY && isFirstTimePlaying){
            isFirstTimePlaying = false
            onLoadedCallback()
        }

        if(playWhenReady){
            //Check if playWhenReady was false and now is true
            if(!isOnPlay && !isFirstTimePlaying){
                isOnPlay = true
                onPlayChanged(isOnPlay)
            }
            if(playbackState == Player.STATE_READY){
                isElapsedUpdating = true
                handler.postDelayed(runnable, updateElapsedDelay);
            }else{
                stopUpdatingElapsed()
            }
        }

        if(!playWhenReady){
            if(isOnPlay){
                isOnPlay = false
                onPlayChanged(isOnPlay)
            }
            if(isElapsedUpdating) stopUpdatingElapsed()
        }
    }

    fun stopUpdatingElapsed(){
        isElapsedUpdating=false
        handler.removeCallbacks(runnable)
    }
}
