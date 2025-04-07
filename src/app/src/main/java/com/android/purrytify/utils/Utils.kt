import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

// Utils func here
fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun extractDominantColor(bitmap: Bitmap): Color {
    val palette = Palette.from(bitmap).generate()
    val dominantSwatch = palette.dominantSwatch
    return if (dominantSwatch != null) Color(dominantSwatch.rgb) else Color.Black
}

fun darkenColor(color: Color, factor: Float = 0.7f): Color {
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}