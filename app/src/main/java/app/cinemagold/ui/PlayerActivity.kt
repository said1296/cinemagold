package app.cinemagold.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentType
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerActivity : AppCompatActivity() {
    private lateinit var player : ExoPlayer
    private lateinit var playerView : PlayerView
    private lateinit var progressiveMediaSource: ProgressiveMediaSource
    private lateinit var contentType : ContentType
    lateinit var contentSource : String
    var contentId : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        if (Build.VERSION.SDK_INT >= 30){
            window.setDecorFitsSystemWindows(false)
        }else{
            val systemUiVisibilityFlags = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

            //Handler and runnable for scheduling hiding of navbar and status bar with delay
            val handler = Handler(Looper.getMainLooper())
            val hideUiRunnable = Runnable {
                window.decorView.systemUiVisibility = systemUiVisibilityFlags
            }

            window.decorView.systemUiVisibility = systemUiVisibilityFlags
            window.decorView.setOnSystemUiVisibilityChangeListener {
                if((window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)==0){
                    //Schedule hiding of navbar and status bar
                    handler.removeCallbacks(hideUiRunnable)
                    handler.postDelayed(hideUiRunnable, 1000)
                }
            }
        }

        //Retrieve data sent from previous activity
        val extras = intent.extras
        if(extras != null){
            contentType = extras.get("contentType") as ContentType
            contentSource = extras.get("contentSource") as String
            contentId = extras.get("contentId") as Int
        }

        player = SimpleExoPlayer.Builder(this).build()
        playerView = findViewById(R.id.player)
        initializePlayer(contentSource)
        playerView.player = player
    }

    private fun initializePlayer(url: String){
        player.repeatMode = Player.REPEAT_MODE_ALL

        val userAgent =
            Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))


        progressiveMediaSource = ProgressiveMediaSource
            .Factory(
                DefaultDataSourceFactory(playerView.context, userAgent),
                DefaultExtractorsFactory()
            )
            .createMediaSource(Uri.parse(url))

        player.prepare(progressiveMediaSource, true, false)
    }
}