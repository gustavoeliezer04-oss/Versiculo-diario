package br.com.gustavoeliezer.versiculodiario

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailyUpdate(this)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF7052A5))) { Surface { VersiculoApp(this) } } }
    }
}

private fun scheduleDailyUpdate(context: Context) {
    val request = PeriodicWorkRequestBuilder<DailyVerseWorker>(24, TimeUnit.HOURS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork("daily_verse_update", ExistingPeriodicWorkPolicy.UPDATE, request)
}

@Composable
private fun VersiculoApp(activity: ComponentActivity) {
    val prefs = remember { activity.getSharedPreferences("versiculo_diario", Context.MODE_PRIVATE) }
    var photoUri by remember { mutableStateOf(prefs.getString("photo_uri", null)?.let(Uri::parse)) }
    var photo by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var verseIndex by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Sem notificações • A tela inicial não será alterada") }
    var editorOpen by remember { mutableStateOf(false) }
    var optionsOpen by remember { mutableStateOf(false) }
    var font by remember { mutableStateOf(prefs.getString("font", "Serifada") ?: "Serifada") }
    var textColor by remember { mutableIntStateOf(prefs.getInt("text_color", android.graphics.Color.WHITE)) }
    var textSize by remember { mutableFloatStateOf(prefs.getFloat("text_size", 28f)) }
    var posX by remember { mutableFloatStateOf(prefs.getFloat("pos_x", 0f)) }
    var posY by remember { mutableFloatStateOf(prefs.getFloat("pos_y", 0f)) }

    fun loadPhoto(uri: Uri) {
        try {
            activity.contentResolver.openInputStream(uri).use { photo = BitmapFactory.decodeStream(it) }
            photoUri = uri
            prefs.edit().putString("photo_uri", uri.toString()).apply()
            editorOpen = true
            status = "Foto carregada. Arraste o texto para posicioná-lo."
        } catch (_: Exception) { status = "Não foi possível abrir essa foto." }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try { activity.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            loadPhoto(uri)
        }
    }

    LaunchedEffect(photoUri) {
        val uri = photoUri
        if (uri != null && photo == null) {
            try { activity.contentResolver.openInputStream(uri).use { photo = BitmapFactory.decodeStream(it) } } catch (_: Exception) { }
        }
    }

    val verse = VerseLibrary.all[verseIndex]
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F2EC)).padding(horizontal = 20.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Versículo Diário", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26352D))
        Text("Uma palavra para acompanhar seu dia.", color = Color(0xFF65736A), fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDD8CE))) {
            if (photo != null) {
                Box(Modifier.fillMaxSize()) {
                    Image(photo!!.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Text(verse.text, Modifier.align(Alignment.Center).padding(18.dp).background(Color(0x77000000), RoundedCornerShape(8.dp)).padding(10.dp), color = Color(textColor), fontSize = (textSize * .55f).sp, fontFamily = font.asFontFamily(), fontWeight = FontWeight.Medium)
                }
            } else {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF526458))
                    Text("Sua foto aparecerá aqui", color = Color(0xFF526458))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(verse.reference, fontWeight = FontWeight.SemiBold, color = Color(0xFF26352D))
        Text(verse.text, fontSize = 14.sp, color = Color(0xFF526458))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) { Text(if (photoUri == null) "Escolher foto" else "Trocar foto") }
            OutlinedButton(onClick = { verseIndex = (verseIndex + 1) % VerseLibrary.all.size }, modifier = Modifier.weight(1f)) { Text("Outro versículo") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (photo != null) editorOpen = true else status = "Escolha uma foto primeiro." }, modifier = Modifier.weight(1f)) { Text("Ajustar posição") }
            OutlinedButton(onClick = { optionsOpen = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Settings, null); Spacer(Modifier.width(5.dp)); Text("Opções") }
        }
        Button(onClick = {
            val uri = photoUri
            if (uri == null) status = "Escolha uma foto primeiro."
            else try {
                val bitmap = WallpaperComposer.create(activity, uri, verse, font, textColor, textSize, posX, posY)
                if (bitmap != null) {
                    WallpaperManager.getInstance(activity).setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    prefs.edit().putInt("lock_wallpaper_id", WallpaperManager.getInstance(activity).getWallpaperId(WallpaperManager.FLAG_LOCK)).putString("last_verse", verse.text).putString("last_reference", verse.reference).apply()
                    bitmap.recycle()
                    status = "Aplicado somente à tela de bloqueio."
                } else status = "Não foi possível abrir essa foto."
            } catch (e: Exception) { status = "Falha ao aplicar: ${e.localizedMessage ?: "verifique a permissão SET_WALLPAPER"}" }
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Lock, null); Spacer(Modifier.width(8.dp)); Text("Aplicar na tela de bloqueio")
        }
        Text(status, fontSize = 12.sp, color = Color(0xFF65736A))
        Text("Sem notificações • A tela inicial não será alterada", fontSize = 11.sp, color = Color(0xFF7D877F))
    }

    if ((editorOpen && photo != null)) {
        Dialog(onDismissRequest = { editorOpen = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                Image(photo!!.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val maxW = constraints.maxWidth
                    val maxH = constraints.maxHeight
                    Text(
                        verse.text,
                        Modifier.align(Alignment.Center).offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    posX += pan.x
                                    posY += pan.y
                                    textSize = (textSize * zoom).coerceIn(14f, 64f)
                                }
                            }
                            .background(Color(0x77000000), RoundedCornerShape(8.dp)).padding(12.dp),
                        color = Color(textColor), fontSize = textSize.sp, fontFamily = font.asFontFamily(), fontWeight = FontWeight.Medium
                    )
                }
                Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(Color(0xCC151515)).padding(12.dp)) {
                    Text("Ajuste o versículo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Arraste para mover • use dois dedos para redimensionar", color = Color.LightGray, fontSize = 12.sp)
                }
                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xCC151515)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { posX = 0f; posY = 0f }, modifier = Modifier.weight(1f)) { Text("Centralizar") }
                    Button(onClick = { prefs.edit().putFloat("pos_x", posX).putFloat("pos_y", posY).putFloat("text_size", textSize).apply(); editorOpen = false }, modifier = Modifier.weight(1f)) { Text("Concluir") }
                }
            }
        }
    }

    if (optionsOpen) {
        AlertDialog(
            onDismissRequest = { optionsOpen = false },
            title = { Text("Opções de aparência") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Fonte")
                    listOf("Serifada", "Sem serifa", "Monoespaçada").forEach { choice ->
                        FilterChip(selected = font == choice, onClick = { font = choice }, label = { Text(choice) })
                    }
                    Text("Cor do texto")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(android.graphics.Color.WHITE, android.graphics.Color.YELLOW, android.graphics.Color.CYAN, android.graphics.Color.LTGRAY).forEach { color ->
                            FilterChip(selected = textColor == color, onClick = { textColor = color }, label = { Text(when(color) { android.graphics.Color.WHITE -> "Branco"; android.graphics.Color.YELLOW -> "Amarelo"; android.graphics.Color.CYAN -> "Azul"; else -> "Cinza" }) })
                        }
                    }
                    Text("Tamanho: ${textSize.toInt()} sp")
                    Slider(value = textSize, onValueChange = { textSize = it }, valueRange = 14f..64f)
                }
            },
            confirmButton = { TextButton(onClick = { prefs.edit().putString("font", font).putInt("text_color", textColor).putFloat("text_size", textSize).apply(); optionsOpen = false }) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = { optionsOpen = false }) { Text("Fechar") } }
        )
    }
}

private fun String.asFontFamily(): FontFamily = when(this) {
    "Sem serifa" -> FontFamily.SansSerif
    "Monoespaçada" -> FontFamily.Monospace
    else -> FontFamily.Serif
}
