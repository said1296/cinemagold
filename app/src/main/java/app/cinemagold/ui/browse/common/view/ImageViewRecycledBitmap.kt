package app.cinemagold.ui.browse.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet


//Support for recycling bitmaps programmatically to avoid OutOfMemoryException
class ImageViewRecycledBitmap @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    override fun onDraw(canvas: Canvas?) {
        if(drawable is BitmapDrawable){
            if((drawable as BitmapDrawable).bitmap.isRecycled)
                return
        }
        super.onDraw(canvas)
    }
}