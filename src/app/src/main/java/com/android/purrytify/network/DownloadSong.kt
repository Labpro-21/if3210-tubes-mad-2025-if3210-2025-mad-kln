import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

suspend fun downloadSong(context: Context, song: Song, userId: Int) {
    val resolver = context.contentResolver

    suspend fun downloadFile(
        fileUrl: String,
        fileName: String,
        mimeType: String,
        directory: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = resolver.insert(
                when (directory) {
                    Environment.DIRECTORY_PICTURES -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                },
                values
            )

            uri?.let {
                val output = resolver.openOutputStream(it)
                val input: InputStream = URL(fileUrl).openStream()

                input.use { i ->
                    output?.use { o ->
                        i.copyTo(o)
                    }
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, values, null, null)
            }

            uri
        } catch (e: Exception) {
            Log.e("Download", "Failed to download file: $fileUrl", e)
            null
        }
    }

    val audioUri = downloadFile(
        fileUrl = song.audioUri,
        fileName = "${song.title}.mp3",
        mimeType = "audio/mpeg",
        directory = Environment.DIRECTORY_MUSIC
    )

    val imageUri = downloadFile(
        fileUrl = song.imageUri,
        fileName = "${song.title}.png",
        mimeType = "image/png",
        directory = Environment.DIRECTORY_PICTURES
    )

    if (audioUri != null && imageUri != null) {
        RepositoryProvider.getSongRepository().insertSong(
            song.copy(
                audioUri = audioUri.toString(),
                imageUri = imageUri.toString(),
                isDownloaded = true,
                uploaderId = userId,
                id = 0,
                rank = 0,
            )
        )
        RepositoryProvider.getSongRepository().updateIsDownloaded(song.title, song.artist, true)
        Log.d("Download", "Song ${song.title} downloaded and saved to DB.")
    } else {
        Log.e("Download", "Download failed, not inserting to DB.")
    }
}
