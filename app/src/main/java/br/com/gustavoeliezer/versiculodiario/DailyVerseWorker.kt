package br.com.gustavoeliezer.versiculodiario

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate

class DailyVerseWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("versiculo_diario", Context.MODE_PRIVATE)
        val uriText = prefs.getString("photo_uri", null) ?: return Result.success()
        return try {
            val manager = WallpaperManager.getInstance(applicationContext)
            val expectedId = prefs.getInt("lock_wallpaper_id", -1)
            // Se a pessoa trocar a tela de bloqueio fora do app, não sobrescrevemos a nova foto.
            if (expectedId == -1 || manager.getWallpaperId(WallpaperManager.FLAG_LOCK) != expectedId) return Result.success()

            val verse = VerseLibrary.all.random()
            val bitmap = WallpaperComposer.create(
                applicationContext,
                Uri.parse(uriText),
                verse,
                prefs.getString("font", "Serifada") ?: "Serifada",
                prefs.getInt("text_color", Color.WHITE),
                prefs.getFloat("text_size", 28f),
                prefs.getFloat("pos_x", 0f),
                prefs.getFloat("pos_y", 0f)
            ) ?: return Result.retry()
            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            prefs.edit()
                .putInt("lock_wallpaper_id", manager.getWallpaperId(WallpaperManager.FLAG_LOCK))
                .putString("last_verse", verse.text)
                .putString("last_reference", verse.reference)
                .putString("last_update", LocalDate.now().toString())
                .apply()
            bitmap.recycle()
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}