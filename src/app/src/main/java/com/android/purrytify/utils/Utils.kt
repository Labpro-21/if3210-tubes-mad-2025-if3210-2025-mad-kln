import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.android.purrytify.datastore.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Locale

// Utils func here
    fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                null
            } else {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("Utils", "Error loading bitmap from: $uriString", e)
            null
        }
    }
    
    fun extractDominantColor(bitmap: Bitmap): Color {
        val palette = Palette.from(bitmap).generate()
        val dominantSwatch = palette.dominantSwatch
        return if (dominantSwatch != null) Color(dominantSwatch.rgb) else Color.Black
    }

suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun getCountryNameFromCode(code: String): String {
    return try {
        Locale("", code).displayCountry
    } catch (e: Exception) {
        code
    }
}

fun darkenColor(color: Color, factor: Float = 0.7f): Color {
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}

suspend fun fetchUserId(context: Context): Int {
    Log.d("DEBUG_PROFILE", "Fetching user ID from Utils.kt")
    return TokenManager.getCurrentId(context).firstOrNull() ?: 0
}

suspend fun getToken(context: Context): String? {
    return TokenManager.getToken(context).firstOrNull()
}