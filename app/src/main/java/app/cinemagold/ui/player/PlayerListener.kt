package app.cinemagold.ui.player

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray


class PlayerListener(val onLoadedCallback : () -> Unit, val updateElapsed : () -> Unit) : Player.EventListener {
    private var isFirstTimePlaying = true
    private val updateElapsedDelay = 5000L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object: Runnable {
        override fun run() {
            updateElapsed()
            handler.postDelayed(this, updateElapsedDelay);
        }
    }
    private var isElapsedUpdating = false

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //Wait for data to be loaded before accessing players' mappedTrackInfo
        if(playbackState==Player.STATE_READY && isFirstTimePlaying){
            isFirstTimePlaying = false
            onLoadedCallback()
        }
        if (playWhenReady && playbackState == Player.STATE_READY) {
            isElapsedUpdating = true
            handler.postDelayed(runnable, updateElapsedDelay);
        }else{
            stopUpdatingElapsed()
        }
        if(!playWhenReady && isElapsedUpdating){
            stopUpdatingElapsed()
        }
    }

    fun stopUpdatingElapsed(){
        isElapsedUpdating=false
        handler.removeCallbacks(runnable)
    }
}