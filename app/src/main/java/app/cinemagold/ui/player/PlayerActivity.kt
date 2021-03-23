package app.cinemagold.ui.player

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import app.cinemagold.model.content.ContentType
import app.cinemagold.model.content.SubtitleType
import app.cinemagold.model.generic.IdAndName
import app.cinemagold.model.user.PlayerAuthorization
import app.cinemagold.ui.authentication.AuthenticationActivity
import app.cinemagold.ui.browse.BrowseActivity
import app.cinemagold.ui.browse.preview.EpisodeRVA
import app.cinemagold.ui.browse.preview.PreviewFragment
import app.cinemagold.ui.option.OptionActivity
import app.cinemagold.ui.option.payment.PaymentFragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
import kotlinx.android.synthetic.main.player_control_movie.view.*
import kotlinx.android.synthetic.main.player_selector.view.*
import javax.inject.Inject
import kotlin.math.roundToInt


class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var mediaInfo: MediaInfo
    private lateinit var sources: MediaSource
    private var mediaTracks = mutableListOf<MediaTrack>()
    private lateinit var dataSourceFactory: DefaultHttpDataSourceFactory
    private val subtitleItems = mutableListOf<IdAndName>()
    private var audioTrackItems: ArrayList<IdAndName> = arrayListOf()
    private lateinit var rootView: ConstraintLayout
    private lateinit var selectorView: LinearLayoutCompat
    private val playerListener = PlayerListener(
        { onLoaded() },
        {
            val elapsedPercent = (player.currentPosition.toFloat() / player.duration)
            viewModel.triggeredUpdateElapsed(player.currentPosition.toInt(), elapsedPercent)
        },
        { isOnPlay ->
            if (isOnPlay)
                viewModel.videoPlaying(player.duration - player.currentPosition)
            else
                viewModel.videoIdle()
        },
        {
            if (viewModel.content.mediaType.id != ContentType.MOVIE.value) {
                viewModel.videoEnded()
                preparePlayer()
                onLoaded()
            }
        }
    )
    private var audioTrackSelected = 0
    private var subtitleSelected = -1
    private var isTelevision = false

    @Inject
    lateinit var playerSelectorRVA: PlayerSelectorRVA

    @Inject
    lateinit var episodeRVA: EpisodeRVA

    @Inject
    lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as ApplicationContextInjector).applicationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        isTelevision = resources.getBoolean(R.bool.isTelevision)
        rootView = findViewById(R.id.player)

        //Observers

        viewModel.title.observe(this) { title ->
            rootView.player_control_title.text = title
        }

        viewModel.authorizationError.observe(this) { authorizationError ->
            player.playWhenReady = false
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setMessage(authorizationError.name)
                .setPositiveButton("Entendido") { _, _ ->
                    when (authorizationError.id) {
                        PlayerAuthorization.SUSPENDED.value -> {
                            navigateToAuthentication()
                        }
                        PlayerAuthorization.MEMBERSHIP_EXPIRED.value -> {
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

        viewModel.finish.observe(this) {
            if(it) navigateToBrowse()
        }

        // Fullscreen
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
        } else {
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
                if ((window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    //Schedule hiding of navbar and status bar
                    handler.removeCallbacks(hideUiRunnable)
                    handler.postDelayed(hideUiRunnable, 1000)
                }
            }
        }

        // Retrieve data sent from previous activity
        viewModel.receivedExtras(intent.extras)

        //Set-up player
        trackSelector = DefaultTrackSelector(this)
        trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this)
            .setExceedRendererCapabilitiesIfNecessary(true)
            .setExceedVideoConstraintsIfNecessary(true)
            .setExceedAudioConstraintsIfNecessary(true)
            .build()
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        (player as SimpleExoPlayer).addAnalyticsListener(EventLogger(trackSelector))
        player.playWhenReady = true
        player.addListener(playerListener)

        playerView = findViewById(R.id.player_playerView)
        playerView.exo_custom_subtitles.setOnClickListener {
            showTrackSelector(resources.getString(R.string.subtitles), subtitleItems, C.TRACK_TYPE_TEXT)
        }
        playerView.exo_custom_audio_track.setOnClickListener {
            showTrackSelector(getString(R.string.language), audioTrackItems, C.TRACK_TYPE_AUDIO)
        }

        if (viewModel.content.mediaType.id == ContentType.MOVIE.value) playerView.exo_custom_chapter.visibility =
            View.GONE
        else playerView.exo_custom_chapter.setOnClickListener {
            showChapterSelector()
        }

        playerView.subtitleView?.setStyle(
            CaptionStyleCompat(
                Color.WHITE,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.BLACK,
                null
            )
        )
        playerView.player = player

        preparePlayer()
    }

    override fun onPause() {
        super.onPause()
        playerListener.stopUpdatingElapsed()
        viewModel.videoIdle()
    }

    override fun onBackPressed() {
        player.release()
        viewModel.videoIdle()
        navigateToPreview(viewModel.content.id, viewModel.content.mediaType.id, viewModel.seasonIndex)
    }

    private fun navigateToPreview(contentId: Int, contentTypeId: Int, seasonIndex: Int) {
        val intent = Intent(this, BrowseActivity::class.java)
        intent.putExtra("FRAGMENT", PreviewFragment::class.simpleName)
        //Send information about where the intent came from
        intent.putExtra(PreviewFragment.EXTRA_CONTENT_ID, contentId)
        intent.putExtra(PreviewFragment.EXTRA_CONTENT_TYPE, contentTypeId)
        if (seasonIndex != -1) intent.putExtra(PreviewFragment.EXTRA_SEASON_INDEX, seasonIndex)
        intent.putExtra("ORIGIN", this::class.simpleName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun onLoaded() {
        //MappedTrackInfo can only be accessed once the player is loaded
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo

        //Prepare options for Audio Track Selector
        var currentTrackGroupLanguage: String? = null
        for (i in 0 until player.rendererCount) {
            if (player.getRendererType(i) == C.TRACK_TYPE_AUDIO) {
                //Handle disable option
                val rendererTrackGroups =
                    mappedTrackInfo!!.getTrackGroups(i)
                for (trackGroupIndex in 0 until rendererTrackGroups.length) {
                    for (formatIndex in 0 until rendererTrackGroups[trackGroupIndex].length) {
                        val language =
                            rendererTrackGroups[trackGroupIndex].getFormat(formatIndex).language?.capitalize()
                        if (currentTrackGroupLanguage != language) {
                            currentTrackGroupLanguage = language
                            audioTrackItems.add(IdAndName(trackGroupIndex, language!!))
                        }
                    }
                }
            } else if (player.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                trackSelector.apply {
                    setParameters(buildUponParameters().setRendererDisabled(i, true))
                }
            }
        }

        //Hide buttons if no options available
        if (subtitleItems.isEmpty()) {
            findViewById<ImageButton>(R.id.exo_custom_subtitles).visibility = View.GONE
        } else {
            //Add disable option at beginning of selector list
            subtitleItems.add(0, IdAndName(-1, getString(R.string.disable)))
        }

        if (audioTrackItems.size <= 1) {
            findViewById<ImageButton>(R.id.exo_custom_audio_track).visibility = View.GONE
        }

        //Show controls
        playerView.player_control.visibility = View.VISIBLE
    }

    override fun onStop() {
        player.release()
        super.onStop()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isTelevision)
            playerView.showController()
        return super.onKeyDown(keyCode, event)
    }

    private fun showSelector(
        rva: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        recyclerLayoutManager: RecyclerView.LayoutManager,
        title: String,
        headerTitle: String? = null,
        spinnerView: View? = null,
        hideTitle: Boolean = false
    ) {
        playerView.visibility = View.GONE
        player.playWhenReady = false

        selectorView = layoutInflater.inflate(R.layout.player_selector, null) as LinearLayoutCompat

        if (hideTitle) selectorView.player_selector_title.visibility = View.GONE
        if(headerTitle != null) selectorView.player_selector_header_title.apply{
            visibility = View.VISIBLE
            text = headerTitle
        }

        if(spinnerView != null) {
            (selectorView.player_selector_header_title.layoutParams as LinearLayoutCompat.LayoutParams).weight = 0.5F
            spinnerView.layoutParams = LinearLayoutCompat.LayoutParams(0, LinearLayoutCompat.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.3F
            }
            selectorView.player_selector_header.apply {
                addView(spinnerView, 0)
            }
        }

        //Close button
        selectorView.player_selector_header_close.setOnClickListener {
            hideSelector()
        }

        //Set width, height and margins programmatically since it does not use the XML values when inflated programmatically
        val selectorLayoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT
        )
        selectorView.layoutParams = selectorLayoutParams

        //Title
        selectorView.player_selector_title.text = title

        // Recyclerview
        selectorView.player_selector_recycler.apply {
            layoutManager = recyclerLayoutManager
            adapter = rva
        }

        rootView.addView(selectorView)
        selectorView.player_selector_header_close.requestFocus()
    }

    private fun showTrackSelector(title: String, items: List<IdAndName>, trackType: Int) {
        playerSelectorRVA.clickHandler = { item ->
            changeItemSelected(item, trackType)
        }
        playerSelectorRVA.setDataset(items)
        playerSelectorRVA.selectedId =
            when (trackType) {
                C.TRACK_TYPE_AUDIO -> audioTrackSelected
                C.TRACK_TYPE_TEXT -> subtitleSelected
                else -> -1
            }

        showSelector(playerSelectorRVA, LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), title)
    }

    private fun showChapterSelector() {
        var spinnerSeasonIndex = viewModel.seasonIndex
        episodeRVA.clickHandler = { position ->
            viewModel.changedEpisode(spinnerSeasonIndex, position)
            hideSelector()
            preparePlayer()
        }
        episodeRVA.isPlayerActivity = true
        episodeRVA.setDataset(viewModel.content.seasons[viewModel.seasonIndex].episodes)


        // Build season spinner
        val spinnerView = layoutInflater.inflate(R.layout.spinner_season_view, null) as Spinner
        val seasonSpinnerItems = mutableListOf<String>()
        for (season in viewModel.content.seasons) {
            seasonSpinnerItems.add("Temporada ${season.number}")
        }
        val adapterSpinner = ArrayAdapter<String>(applicationContext, R.layout.spinner_season, seasonSpinnerItems)
        adapterSpinner.setDropDownViewResource(R.layout.spinner_season_item)
        spinnerView.apply {
            adapter = adapterSpinner
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    spinnerSeasonIndex = p2
                    episodeRVA.setDataset(viewModel.content.seasons[spinnerSeasonIndex].episodes)
                }
            }
            setSelection(viewModel.seasonIndex)
            nextFocusRightId = R.id.player_selector_header_close
        }

        val screenWidth = windowManager.defaultDisplay.width
        val numberOfColumns = (screenWidth / resources.getDimensionPixelSize(R.dimen.item_episode_horizontal_width)).toDouble().roundToInt()

        showSelector(
            episodeRVA,
            if (isTelevision) LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            else GridLayoutManager(this, numberOfColumns),
            "Episodios",
            viewModel.content.seasons[viewModel.seasonIndex].episodes[viewModel.episodeIndex].name,
            spinnerView,
            true
        )
    }

    private fun hideSelector() {
        playerView.visibility = View.VISIBLE
        player.playWhenReady = true
        if (this::selectorView.isInitialized) {
            rootView.removeView(selectorView)
        }
    }

    private fun changeItemSelected(item: IdAndName, trackType: Int) {
        when (trackType) {
            C.TRACK_TYPE_AUDIO -> audioTrackSelected = item.id
            C.TRACK_TYPE_TEXT -> subtitleSelected = item.id
            else -> {
            }
        }


        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        for (i in 0 until player.rendererCount) {
            if (player.getRendererType(i) == trackType) {
                //Handle disable option
                if (item.id == -1) {
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
                    setParameters(
                        buildUponParameters()
                            .setRendererDisabled(i, false)
                            .setSelectionOverride(i, rendererTrackGroups, selectionOverride)
                    )
                }
                break
            }
        }
        hideSelector()
    }


    private fun preparePlayer() {
        // Load from 0
        playerListener.isFirstTimePlaying = true

        //Hide controls until video is loaded
        playerView.player_control.visibility = View.GONE
        val userAgent =
            Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))
        val contentSource =
            if (viewModel.content.mediaType.id == ContentType.MOVIE.value) {
                viewModel.content.movie.src
            } else {
                viewModel.content.seasons[viewModel.seasonIndex].episodes[viewModel.episodeIndex].src
            }
        dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        prepareVideo(contentSource)
        prepareSubtitles()
        prepareCast(contentSource)
        player.prepare(sources, false, false)
        if (viewModel.elapsed != -1L) {
            player.seekTo(viewModel.elapsed)
            // Restart elapsed so when chapter changes it won't seekTo last chapter's elapsed value
            viewModel.elapsed = -1L
        } else {
            player.seekTo(0)
        }
    }

    private fun prepareCast(contentSource: String) {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, viewModel.content.name)
        mediaInfo = MediaInfo.Builder(contentSource).apply {
            setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            setContentType(MimeTypes.APPLICATION_M3U8)
            setMetadata(movieMetadata)
            if (mediaTracks.isNotEmpty())
                setMediaTracks(mediaTracks)
        }.build()
    }

    private fun prepareVideo(contentSource: String) {
        //Handle if HLS or MP4
        sources =
            if (contentSource.endsWith("m3u8")) {
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(Uri.parse(contentSource))
            } else {
                ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(contentSource))
            }
    }

    private fun prepareSubtitles() {
        var subtitleSource: MediaSource
        var subtitleFormat: Format

        val subtitles =
            if (viewModel.content.mediaType.id == ContentType.MOVIE.value) {
                viewModel.content.movie.subtitles
            } else {
                viewModel.content.seasons[viewModel.seasonIndex].episodes[viewModel.episodeIndex].subtitles
            }

        //Create list for subtitle selector
        for ((index, subtitle) in subtitles.withIndex()) {
            subtitleItems.apply {
                if (SubtitleType.from(subtitle.subtitleType.id) == SubtitleType.NORMAL) {
                    add(IdAndName(index, subtitle.language.name))
                } else {
                    add(IdAndName(index, subtitle.language.name + " [" + getString(R.string.forced) + "]"))
                }
            }
        }

        //Add subtitles to player sources
        for (subtitle in subtitles) {
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
    private fun navigateToAuthentication() {
        val intent = Intent(applicationContext, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun navigateToOption(fragmentToLoad: String, isEdit: Boolean? = null) {
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

    fun navigateToBrowse(){
        val intent = Intent(applicationContext, BrowseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRAS_SEASON_INDEX = "SEASON_INDEX"
        const val EXTRAS_EPISODE_INDEX = "EPISODE_INDEX"
        const val EXTRAS_CONTENT = "CONTENT_INDEX"
        const val EXTRAS_ELAPSED = "ELAPSED"
    }
}
