package br.com.gustavoeliezer.versiculodiario

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import kotlin.math.min

object WallpaperComposer {
    fun create(
        context: Context, uri: Uri, verse: Verse,
        font: String = "Serifada", textColor: Int = Color.WHITE,
        textSize: Float = 28f, offsetX: Float = 0f, offsetY: Float = 0f
    ): Bitmap? {
        val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        source.recycle()
        val canvas = Canvas(result)
        val width = result.width.toFloat()
        val height = result.height.toFloat()
        val scale = (width / 420f).coerceAtLeast(0.65f)
        val typeface = when (font) {
            "Sem serifa" -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            "Monoespaçada" -> Typeface.create("monospace", Typeface.NORMAL)
            else -> Typeface.create("serif", Typeface.NORMAL)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            this.typeface = typeface
            this.textSize = textSize * scale
            setShadowLayer(5f * scale, 1f, 2f, Color.BLACK)
        }
        val referencePaint = Paint(textPaint).apply {
            textSize = textPaint.textSize * 0.55f
            color = textColor
        }
        val maxWidth = width * 0.86f
        val lines = wrapText(verse.text, textPaint, maxWidth)
        val lineHeight = textPaint.fontSpacing * 1.15f
        val blockHeight = lines.size * lineHeight + referencePaint.fontSpacing + 24f * scale
        val centerX = width / 2f + offsetX * scale
        val centerY = height * 0.72f + offsetY * scale
        val top = centerY - blockHeight / 2f
        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(112, 0, 0, 0) }
        canvas.drawRoundRect(RectF(width * 0.04f + offsetX * scale, top - 16f * scale, width * 0.96f + offsetX * scale, top + blockHeight + 16f * scale), 24f * scale, 24f * scale, panel)
        var y = top - textPaint.ascent()
        lines.forEach { line -> canvas.drawText(line, centerX, y, textPaint); y += lineHeight }
        y += referencePaint.textSize * 0.7f
        canvas.drawText(verse.reference, centerX, y, referencePaint)
        return result
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var line = ""
        text.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) { lines += line; line = word }
            else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        return lines
    }
}