package app.cinemagold.ui.common

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class ContentItemTarget(val resources: Resources) : Target {
    val stateListDrawable = StateListDrawable()
    var defaultDrawable : Drawable = ColorDrawable(Color.TRANSPARENT)
    var focusedDrawable : Drawable = ColorDrawable(Color.TRANSPARENT)
    var loadedCallback : ((Drawable) -> Unit)? = null

    constructor(resources: Resources, loadedCallback: (Drawable) -> Unit) : this(resources){
        this.loadedCallback = loadedCallback
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        defaultDrawable = BitmapDrawable(resources, bitmap)
        focusedDrawable = defaultDrawable.constantState!!.newDrawable().mutate()
        focusedDrawable.apply {
            alpha = 50
        }
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_focused), defaultDrawable)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), focusedDrawable)
        loadedCallback?.invoke(stateListDrawable)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
    }
}
