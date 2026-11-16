package br.com.gustavoeliezer.versiculodiario

import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailyUpdate(this)
        setContent { MaterialTheme { Surface { VersiculoApp(this) } } }
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
    var preview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var verseIndex by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Escolha uma foto para começar.") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try { activity.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            photoUri = uri
            prefs.edit().putString("photo_uri", uri.toString()).apply()
            preview = WallpaperComposer.create(activity, uri, VerseLibrary.all[verseIndex])
            status = "Foto carregada. Confira a prévia."
        }
    }

    LaunchedEffect(photoUri, verseIndex) {
        val uri = photoUri
        if (uri != null) preview = WallpaperComposer.create(activity, uri, VerseLibrary.all[verseIndex])
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F2EC)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Versículo Diário", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26352D))
        Text("Uma palavra para acompanhar seu dia.", color = Color(0xFF65736A), fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDD8CE))) {
            if (preview != null) {
                Image(preview!!.asImageBitmap(), "Prévia do papel de parede com versículo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF526458))
                    Spacer(Modifier.height(8.dp))
                    Text("Sua foto aparecerá aqui", color = Color(0xFF526458))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(VerseLibrary.all[verseIndex].reference, fontWeight = FontWeight.SemiBold, color = Color(0xFF26352D))
        Text(VerseLibrary.all[verseIndex].text, fontSize = 14.sp, color = Color(0xFF526458))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) {
                Text(if (photoUri == null) "Escolher foto" else "Trocar foto")
            }
            OutlinedButton(onClick = { verseIndex = (verseIndex + 1) % VerseLibrary.all.size }, modifier = Modifier.weight(1f)) { Text("Outro versículo") }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val uri = photoUri
            if (uri == null) status = "Escolha uma foto primeiro."
            else try {
                val bitmap = WallpaperComposer.create(activity, uri, VerseLibrary.all[verseIndex])
                if (bitmap != null) {
                    WallpaperManager.getInstance(activity).setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    prefs.edit().putInt("lock_wallpaper_id", WallpaperManager.getInstance(activity).getWallpaperId(WallpaperManager.FLAG_LOCK)).putString("last_verse", VerseLibrary.all[verseIndex].text).putString("last_reference", VerseLibrary.all[verseIndex].reference).apply()
                    bitmap.recycle()
                    status = "Aplicado somente à tela de bloqueio!"
                } else status = "Não foi possível abrir essa foto."
            } catch (e: Exception) {
                status = "Falha ao aplicar: " + (e.localizedMessage ?: "erro desconhecido")
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Aplicar na tela de bloqueio")
        }
        Spacer(Modifier.height(8.dp))
        Text(status, fontSize = 12.sp, color = Color(0xFF65736A))
        Spacer(Modifier.height(4.dp))
        Text("Sem notificações • A tela inicial não será alterada", fontSize = 11.sp, color = Color(0xFF7D877F))
    }
}
