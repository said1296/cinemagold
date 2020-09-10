package app.cinemagold.ui.player

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.Content
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.SubtitleType
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.PlayerAuthorization
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.payment.PaymentFragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_player.view.*
import kotlinx.android.synthetic.main.player_control_movie.view.*
import kotlinx.android.synthetic.main.player_selector.view.*
import java.util.stream.Collectors
import javax.inject.Inject


class PlayerActivity : AppCompatActivity() {
    private lateinit var player : ExoPlayer
    private lateinit var playerView : PlayerView
    private lateinit var content : Content
    private var seasonIndex : Int = 0
    private var episodeIndex : Int = 0
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var  mediaInfo: MediaInfo
    private lateinit var sources : MediaSource
    private var mediaTracks = mutableListOf<MediaTrack>()
    private lateinit var dataSourceFactory  : DefaultHttpDataSourceFactory
    private lateinit var subtitleItems : List<IdAndName>
    private var audioTrackItems : ArrayList<IdAndName> = arrayListOf()
    private lateinit var rootView : ConstraintLayout
    private lateinit var selectorView : LinearLayoutCompat
    private val playerListener = PlayerListener (
        { onLoaded() },
        {
            val elapsedPercent = (player.currentPosition.toFloat() / player.duration)
            if(content.mediaType.id== ContentType.MOVIE.value){
                playerViewModel.triggeredUpdateElapsed(content.id, player.currentPosition.toInt(), elapsedPercent)
            }else{
                playerViewModel.triggeredUpdateElapsed(content.id,
                    content.seasons[seasonIndex].episodes[episodeIndex].id, player.currentPosition.toInt(), elapsedPercent)
            }
        },
        {isOnPlay ->
            if(isOnPlay)
                playerViewModel.videoPlaying(player.duration - player.currentPosition, content.mediaType.id)
            else
                playerViewModel.videoPaused()
        }
    )
    private var audioTrackSelected = 0
    private var subtitleSelected = -1
    @Inject
    lateinit var playerSelectorRVA : PlayerSelectorRVA
    @Inject
    lateinit var playerViewModel: PlayerViewModel
    lateinit var castPlayer: CastPlayer
    lateinit var castContext: CastContext
    var elapsed = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        rootView = findViewById(R.id.player)

        //Observers
        playerViewModel.authorizationError.observe(this){authorizationError ->
            player.playWhenReady = false
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setMessage(authorizationError.name)
                .setPositiveButton("Entendido") { _, _ ->
                    when(authorizationError.id){
                        PlayerAuthorization.SUSPENDED.value -> {
                            navigateToAuthentication()
                        }
                        PlayerAuthorization.MEMBERSHIP_EXPIRED.value-> {
                            navigateToOption(PaymentFragment::class.simpleName!!)
                        }
                        PlayerAuthorization.DEVICE_LIMIT_REACHED.value -> {
                            this.onBackPressed()
                        }
                        PlayerAuthorization.CONTENT_TYPE_PERMISSION_DENIED.value -> {
                            this.onBackPressed()
                        }
                    }
                }
                .create().show()
        }

        //Cast
        val mediaRouteButton = rootView.media_route_button
        val castControlView = rootView.cast_control_view
        CastButtonFactory.setUpMediaRouteButton(applicationContext, mediaRouteButton)

        if(!resources.getBoolean(R.bool.isTelevision)){
            castContext = CastContext.getSharedInstance(this)
            castPlayer = CastPlayer(castContext)

            if (castContext.castState != CastState.NO_DEVICES_AVAILABLE) mediaRouteButton.visibility = View.VISIBLE
            castContext.addCastStateListener { state ->
                if (state == CastState.NO_DEVICES_AVAILABLE) mediaRouteButton.visibility = View.GONE else {
                    if (mediaRouteButton.visibility == View.GONE) mediaRouteButton.visibility = View.VISIBLE
                }
            }

            //TODO: VIEW
            /*castControlView.player = castPlayer*/
            castControlView.visibility = View.GONE
        }

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
            if(content.mediaType.id != ContentType.MOVIE.value){
                episodeIndex = extras.getInt("episodeIndex")
                seasonIndex = extras.getInt("seasonIndex")
            }
            //Get elapsed time if a Recently Played content was clicked
            if(extras.getInt("elapsed") != -1){
                elapsed = extras.getInt("elapsed").toLong()
            }
        }

        //Set-up player
        trackSelector = DefaultTrackSelector(this)
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        player.playWhenReady = true
        player.addListener(playerListener)

        playerView = findViewById(R.id.player_playerView)
        //Hide controls until video is loaded
        playerView.player_control.visibility = View.GONE
        playerView.exo_custom_subtitles.setOnClickListener {
            showSelector(resources.getString(R.string.subtitles), subtitleItems, C.TRACK_TYPE_TEXT)
        }
        playerView.exo_custom_audio_track.setOnClickListener {
            showSelector(getString(R.string.language), audioTrackItems, C.TRACK_TYPE_AUDIO)
        }
        playerView.player = player

        preparePlayer()
    }

    override fun onPause() {
        super.onPause()
        playerListener.stopUpdatingElapsed()
        playerViewModel.videoPaused()
    }

    private fun onLoaded(){
        //MappedTrackInfo can only be accessed once the player is loaded
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo

        //Prepare options for Audio Track Selector
        var currentTrackGroupLanguage : String? = null
        for(i in 0 until player.rendererCount){
            if(player.getRendererType(i)==C.TRACK_TYPE_AUDIO){
                //Handle disable option
                val rendererTrackGroups =
                    mappedTrackInfo!!.getTrackGroups(i)
                for(trackGroupIndex in 0 until rendererTrackGroups.length){
                    for(formatIndex in 0 until rendererTrackGroups[trackGroupIndex].length){
                        val language = rendererTrackGroups[trackGroupIndex].getFormat(formatIndex).language?.capitalize()
                        if(currentTrackGroupLanguage != language){
                            currentTrackGroupLanguage = language
                            audioTrackItems.add(IdAndName(trackGroupIndex, language!!))
                        }
                    }
                }
            }else if(player.getRendererType(i)==C.TRACK_TYPE_TEXT){
                trackSelector.apply {
                    setParameters(buildUponParameters().setRendererDisabled(i, true))
                }
            }
        }

        //Hide buttons if no options available
        if(subtitleItems.isEmpty()){
            findViewById<ImageButton>(R.id.exo_custom_subtitles).visibility = View.GONE
        }else{
            //Add disable option at beginning of selector list
            subtitleItems = listOf(IdAndName(-1, getString(R.string.disable))) + subtitleItems
        }
        if(audioTrackItems.size <= 1){
            findViewById<ImageButton>(R.id.exo_custom_audio_track).visibility = View.GONE
        }

        //Show controls
        playerView.player_control.visibility = View.VISIBLE
    }

    override fun onStop() {
        player.release()
        super.onStop()
    }

    private fun showSelector(title : String, items : List<IdAndName>, trackType : Int){
        playerView.visibility = View.GONE
        player.playWhenReady = false

        selectorView = layoutInflater.inflate(R.layout.player_selector, null) as LinearLayoutCompat

        //Close button
        selectorView.player_selector_close.setOnClickListener {
            hideSelector()
        }

        //Set width, height and margins programmatically since it does not use the XML values when inflated programmatically
        val selectorLayoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT)
        selectorView.layoutParams = selectorLayoutParams
        selectorView.gravity = Gravity.CENTER

        //Title
        selectorView.player_selector_title.text = title

        //Items recyclerview
        selectorView.player_selector_recycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            adapter = playerSelectorRVA
        }
        playerSelectorRVA.clickHandler = {item ->
            changeItemSelected(item, trackType)
        }
        playerSelectorRVA.setDataset(items)
        playerSelectorRVA.selectedId =
            when(trackType) {
                C.TRACK_TYPE_AUDIO -> audioTrackSelected
                C.TRACK_TYPE_TEXT -> subtitleSelected
                else -> -1
            }

        rootView.addView(selectorView)
    }

    private fun hideSelector(){
        playerView.visibility = View.VISIBLE
        player.playWhenReady = true
        if(this::selectorView.isInitialized){
            rootView.removeView(selectorView)
        }
    }

    private fun changeItemSelected(item : IdAndName, trackType : Int){
        when(trackType) {
            C.TRACK_TYPE_AUDIO -> audioTrackSelected = item.id
            C.TRACK_TYPE_TEXT -> subtitleSelected = item.id
            else -> {}
        }


        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        for(i in 0 until player.rendererCount){
            if(player.getRendererType(i)==trackType){
                //Handle disable option
                if(item.id == -1){
                    trackSelector.apply {
                        setParameters(buildUponParameters().setRendererDisabled(i, true))
                    }

                    break
                }
                val rendererTrackGroups =
                    mappedTrackInfo!!.getTrackGroups(i)

                //Tracks in track group to be used
                val tracks = IntArray(rendererTrackGroups[item.id].length) { it }
                val selectionOverride = SelectionOverride(item.id, *tracks)
                trackSelector.apply {
                    setParameters(buildUponParameters()
                        .setRendererDisabled(i, false)
                        .setSelectionOverride(i, rendererTrackGroups, selectionOverride)
                    )
                }
                break
            }
        }
        hideSelector()
    }


    private fun preparePlayer(){
        val userAgent =
            Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))
        val contentSource =
            if(content.mediaType.id == ContentType.MOVIE.value){
                content.movie.src
            }else{
                content.seasons[seasonIndex].episodes[episodeIndex].src
            }
        dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        prepareVideo(contentSource)
        prepareSubtitles()
        prepareCast(contentSource)
        player.prepare(sources, false, false)
        if(elapsed != -1L){
            player.seekTo(elapsed)
        }
    }

    private fun prepareCast(contentSource : String){
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, content.name)
        mediaInfo = MediaInfo.Builder(contentSource).apply {
            setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            setContentType(MimeTypes.APPLICATION_M3U8)
            setMetadata(movieMetadata)
            if(mediaTracks.isNotEmpty())
                setMediaTracks(mediaTracks)
        }.build()
    }

    private fun prepareVideo(contentSource : String){
        //Handle if HLS or MP4
        sources =
            if(contentSource.endsWith("m3u8")){
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(Uri.parse(contentSource))
            }else{
                ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(contentSource))
            }
    }

    private fun prepareSubtitles(){
        var subtitleSource : MediaSource
        var subtitleFormat : Format

        val subtitles =
            if(content.mediaType.id == ContentType.MOVIE.value){
                content.movie.subtitles
            }else{
                content.seasons[seasonIndex].episodes[episodeIndex].subtitles
            }

        //Create list for subtitle selector
        var index = -1
        this.subtitleItems =  subtitles.stream().map { subtitle ->
                index += 1
                if(SubtitleType.from(subtitle.subtitleType.id) == SubtitleType.NORMAL){
                    IdAndName(index, subtitle.language.name)
                }else{
                    IdAndName(index, subtitle.language.name + "[" + getString(R.string.forced) + "]")
                }
            }.collect(Collectors.toList())

        //Add subtitles to player sources
        for(subtitle in subtitles){
            subtitleFormat = Format.createTextSampleFormat(
                null,
                MimeTypes.TEXT_VTT,
                C.SELECTION_FLAG_DEFAULT,
                subtitle.language.name
            )
            subtitleSource = SingleSampleMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(subtitle.src), subtitleFormat, C.TIME_UNSET)
            sources = MergingMediaSource(sources, subtitleSource)

            mediaTracks.add(
                MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                    .setName(subtitle.language.name)
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(subtitle.src) /* language is required for subtitle type but optional otherwise */
                    .setLanguage(subtitle.language.name)
                    .build()
            )
        }
    }

    //Navigation
    fun navigateToAuthentication() {
        val intent = Intent(applicationContext, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    fun navigateToOption(fragmentToLoad : String, isEdit: Boolean? = null){
        val intent = Intent(this, OptionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("FRAGMENT", fragmentToLoad)
        //Send information about where the intent came from
        intent.putExtra("ORIGIN", this::class.simpleName)
        isEdit?.also { intent.putExtra("IS_EDIT", isEdit) }
        startActivity(intent)
        finish()
    }
}
