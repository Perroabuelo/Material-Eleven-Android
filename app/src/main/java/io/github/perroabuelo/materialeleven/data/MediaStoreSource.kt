package io.github.perroabuelo.materialeleven.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import io.github.perroabuelo.materialeleven.core.library.AudioFormat
import io.github.perroabuelo.materialeleven.core.library.LibraryFilter
import io.github.perroabuelo.materialeleven.core.library.TagText
import io.github.perroabuelo.materialeleven.core.library.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** The songs on the phone, read from the system's music collection. */
class MediaStoreSource(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    /**
     * The listed tracks, emitted once at start and again whenever the collection changes (files
     * copied or deleted), with bursts of changes coalesced.
     */
    @OptIn(FlowPreview::class)
    fun tracks(): Flow<List<Track>> = changes()
        .debounce(CHANGE_DEBOUNCE_MS)
        .conflate()
        .map { query() }
        .flowOn(Dispatchers.IO)

    private fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(COLLECTION, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    private fun query(): List<Track> {
        val cursor = resolver.query(COLLECTION, PROJECTION, "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, null)
            ?: return emptyList()
        return cursor.use { it.readTracks() }
    }

    private fun Cursor.readTracks(): List<Track> {
        val id = getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val title = getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artist = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val album = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumId = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val duration = getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val mime = getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
        val name = getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val added = getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        val folder = getColumnIndexOrThrow(FOLDER_COLUMN)

        val tracks = ArrayList<Track>(count)
        while (moveToNext()) {
            val fileName = getString(name) ?: continue
            val mimeType = getString(mime)
            val folderPath = getString(folder)?.let { if (Build.VERSION.SDK_INT >= 29) it else it.substringBeforeLast('/') + "/" }
            if (!LibraryFilter.isListed(folderPath, fileName, mimeType)) continue

            tracks += Track(
                id = getLong(id),
                title = TagText.title(getString(title), fileName),
                artist = TagText.clean(getString(artist)),
                album = TagText.clean(getString(album)),
                albumId = getLong(albumId),
                durationMs = getLong(duration),
                format = AudioFormat.of(fileName, mimeType),
                fileName = fileName,
                dateAddedSeconds = getLong(added),
            )
        }
        Log.d(TAG, "Listed ${tracks.size} of $count music files")
        return tracks
    }

    companion object {
        val COLLECTION: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        fun uriOf(trackId: Long): Uri = ContentUris.withAppendedId(COLLECTION, trackId)

        private const val TAG = "MediaStoreSource"
        private const val CHANGE_DEBOUNCE_MS = 300L

        // RELATIVE_PATH exists from API 29; before it, the folder comes from the absolute DATA path.
        @Suppress("DEPRECATION")
        private val FOLDER_COLUMN =
            if (Build.VERSION.SDK_INT >= 29) MediaStore.Audio.Media.RELATIVE_PATH else MediaStore.Audio.Media.DATA

        private val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            FOLDER_COLUMN,
        )
    }
}
