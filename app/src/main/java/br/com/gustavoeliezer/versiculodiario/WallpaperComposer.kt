package br.com.gustavoeliezer.versiculodiario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import kotlin.math.min

object WallpaperComposer {
    fun create(context: Context, uri: Uri, verse: Verse): Bitmap? {
        val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val width = result.width.toFloat()
        val height = result.height.toFloat()
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(112, 0, 0, 0) }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            textSize = min(width * 0.052f, 64f)
            setShadowLayer(5f, 1f, 2f, Color.BLACK)
        }
        val referencePaint = Paint(textPaint).apply {
            textSize = min(width * 0.038f, 46f)
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }
        val lines = wrapText(verse.text, textPaint, width * 0.82f)
        val lineHeight = textPaint.fontSpacing * 1.18f
        val totalHeight = lines.size * lineHeight + referencePaint.fontSpacing + 70f
        val top = height * 0.72f - totalHeight / 2f
        canvas.drawRoundRect(RectF(width * 0.055f, top - 34f, width * 0.945f, top + totalHeight + 34f), 28f, 28f, panelPaint)
        var y = top - textPaint.ascent()
        lines.forEach { line ->
            canvas.drawText(line, width / 2f, y, textPaint)
            y += lineHeight
        }
        y += 18f
        canvas.drawText(verse.reference, width / 2f, y, referencePaint)
        return result
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var line = ""
        text.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                lines += line
                line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        return lines
    }
}
