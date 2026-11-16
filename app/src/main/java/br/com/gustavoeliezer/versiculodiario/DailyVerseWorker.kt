package br.com.gustavoeliezer.versiculodiario

import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate

class DailyVerseWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("versiculo_diario", Context.MODE_PRIVATE)
        val uriText = prefs.getString("photo_uri", null) ?: return Result.success()
        return try {
            val verse = VerseLibrary.all.random()
            val bitmap = WallpaperComposer.create(applicationContext, Uri.parse(uriText), verse) ?: return Result.retry()
            WallpaperManager.getInstance(applicationContext).setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            prefs.edit().putString("last_verse", verse.text)
                .putString("last_reference", verse.reference)
                .putString("last_update", LocalDate.now().toString()).apply()
            bitmap.recycle()
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
