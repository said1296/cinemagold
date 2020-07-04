package app.cinemagold.ui.player

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.Subtitle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson


class PlayerActivity : AppCompatActivity() {
    private lateinit var player : ExoPlayer
    private lateinit var playerView : PlayerView
    private lateinit var content : Content
    private lateinit var trackSelector: DefaultTrackSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        //Fullscreen
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
            content = Gson().fromJson(extras.get("content") as String, Content::class.java)
        }

        //Set-up player
        trackSelector = DefaultTrackSelector(this)
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        playerView = findViewById(R.id.player)
        initializePlayer()
        playerView.player = player
    }


    private fun initializePlayer(){
        val contentSource : String
        val subtitles : List<Subtitle>

        player.playWhenReady = true

        //Prepare content data
        if(content.mediaType.id == ContentType.MOVIE.value){
            contentSource = content.movie.src
            subtitles = content.movie.subtitles
        }else{
            contentSource = content.seasons[0].episodes[0].src
            subtitles = content.seasons[0].episodes[0].subtitles
        }

        player.repeatMode = Player.REPEAT_MODE_ALL

        val userAgent =
            Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))
        val dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        var sources : MediaSource

        //Handle HLS or MP4 formats
        if(contentSource.endsWith("m3u8")){
            sources = HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(Uri.parse(contentSource))
        }else{
            sources = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(contentSource))
        }

        //Handle subtitles
        val subtitleSources = arrayListOf<MediaSource>()
        var subtitleSource : MediaSource
        var subtitleFormat : Format
        val subtitleMediaFactory = SingleSampleMediaSource.Factory(dataSourceFactory)
        for((index, subtitle) in subtitles.withIndex()){
            subtitleFormat = Format.createTextSampleFormat(
                index.toString(), MimeTypes.TEXT_VTT, Format.NO_VALUE, subtitle.language.name
            )
            subtitleSource = (subtitleMediaFactory.createMediaSource(Uri.parse(subtitle.src), subtitleFormat, C.TIME_UNSET))
            sources = MergingMediaSource(sources, subtitleSource)
        }

        player.prepare(sources, false, false)
    }
}