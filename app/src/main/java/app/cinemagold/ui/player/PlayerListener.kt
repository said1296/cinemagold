package app.cinemagold.ui.player

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray


class PlayerListener(val onLoadedCallback : () -> Unit) : Player.EventListener {
    private var isFirstTimePlaying = true

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //Wait for data to be loaded before accessing players' mappedTrackInfo
        if(playbackState==3 && isFirstTimePlaying){
            isFirstTimePlaying = false
            onLoadedCallback()
        }
        super.onPlayerStateChanged(playWhenReady, playbackState)
    }
}