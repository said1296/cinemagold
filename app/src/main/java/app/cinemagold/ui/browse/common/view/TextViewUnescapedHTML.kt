package app.cinemagold.ui.browse.common.view

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.AttributeSet


//Unescape HTML characters such as &amp; or &quot;
class TextViewUnescapedHTML @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    override fun setText(text: CharSequence?, type: BufferType?) {
        val textUnescaped : Spanned =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text.toString(), Html.FROM_HTML_MODE_LEGACY)
            } else{
                Html.fromHtml(text.toString())
            }
        super.setText(textUnescaped, BufferType.SPANNABLE)
    }
}