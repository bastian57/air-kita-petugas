package com.daftech.airkita.petugas.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.daftech.airkita.petugas.data.Repository
import com.daftech.airkita.petugas.data.entity.Customer
import com.daftech.airkita.petugas.ui.components.GradientHeader
import com.daftech.airkita.petugas.ui.components.InfoRow
import com.daftech.airkita.petugas.ui.theme.AirBlue
import com.daftech.airkita.petugas.ui.theme.Amber
import com.daftech.airkita.petugas.ui.theme.AppBg
import com.daftech.airkita.petugas.ui.theme.CardBg
import com.daftech.airkita.petugas.ui.theme.Green
import com.daftech.airkita.petugas.ui.theme.Muted
import com.daftech.airkita.petugas.ui.theme.Navy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

/**
 * Catat meter pelanggan: tab Ketik Manual / Scan Kamera (OCR on-device).
 */
@Composable
fun RecordScreen(repo: Repository, nav: NavController, customerId: Long) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var customer by remember { mutableStateOf<Customer?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var digits by remember { mutableStateOf("") }

    LaunchedEffect(customerId) {
        customer = repo.getCustomer(customerId)
        repo.readingFor(customerId)?.let { digits = it.meterEnd.toString() }
    }

    val c = customer
    val meterStart = c?.lastMeter ?: 0
    val meterEnd = digits.toIntOrNull()
    val usage = if (meterEnd != null && meterEnd >= meterStart) meterEnd - meterStart else null
    val suspicious = usage != null && usage > 50

    fun save() {
        val end = meterEnd
        if (c == null || end == null) {
            Toast.makeText(ctx, "Isi angka meter dulu.", Toast.LENGTH_SHORT).show()
            return
        }
        if (end < meterStart) {
            Toast.makeText(ctx, "Meter akhir lebih kecil dari meter awal!", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            repo.saveReading(c.id, end)
            Toast.makeText(ctx, "Tersimpan: ${c.name} → $end m³", Toast.LENGTH_SHORT).show()
            nav.popBackStack()
        }
    }

    Scaffold(containerColor = AppBg) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            GradientHeader(
                "Catat Meter",
                c?.let { "${it.name} • ${it.meterNo}" } ?: "Memuat…",
                slim = true
            )
            TabRow(selectedTabIndex = tab, containerColor = CardBg, contentColor = AirBlue) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("⌨️ Ketik Manual") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("📷 Scan Kamera") })
            }
            if (tab == 0) {
                // ---- MANUAL ----
                Column(Modifier.fillMaxSize().padding(14.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            InfoRow("Meter awal", "$meterStart m³")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Meter akhir",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy,
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                            )
                            OutlinedTextField(
                                value = digits,
                                onValueChange = { digits = it.filter { ch -> ch.isDigit() }.take(7) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("cth: 1450", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 40.sp, fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center, color = Navy
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            when {
                                usage == null -> Text(
                                    "Isi meter akhir untuk melihat pemakaian.",
                                    fontSize = 12.sp, color = Muted,
                                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                                )
                                suspicious -> Text(
                                    "⚠️ Pemakaian $usage m³ — lonjakan tidak wajar, cek ulang!",
                                    fontSize = 13.sp, color = Amber, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                                )
                                else -> Text(
                                    "Pemakaian: $usage m³ ✓",
                                    fontSize = 14.sp, color = Green, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { nav.popBackStack() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Batal") }
                        Button(
                            onClick = { save() },
                            enabled = meterEnd != null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                        ) { Text("💾 Simpan", fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                // ---- SCAN KAMERA ----
                ScanTab(
                    onDigits = { digits = it },
                    onDone = { save() },
                    onCancel = { nav.popBackStack() }
                )
            }
        }
    }
}

/** Tab scan: CameraX + ML Kit OCR, hasil masuk ke state digits. */
@Composable
private fun ScanTab(
    onDigits: (String) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var hasPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPerm = granted }
    LaunchedEffect(Unit) {
        if (!hasPerm) permLauncher.launch(Manifest.permission.CAMERA)
    }

    var digits by remember { mutableStateOf("") }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var providerRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            providerRef?.unbindAll()
            cameraExecutor.shutdown()
            try { recognizer.close() } catch (_: Exception) {}
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (hasPerm) {
                AndroidView(
                    factory = { c ->
                        val pv = PreviewView(c)
                        val future = ProcessCameraProvider.getInstance(c)
                        future.addListener({
                            val provider = future.get()
                            providerRef = provider
                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(pv.surfaceProvider)
                            var lastTs = 0L
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(cameraExecutor) { proxy ->
                                val now = SystemClock.elapsedRealtime()
                                val media = proxy.image
                                if (now - lastTs > 900 && media != null) {
                                    lastTs = now
                                    val image = InputImage.fromMediaImage(
                                        media, proxy.imageInfo.rotationDegrees
                                    )
                                    recognizer.process(image)
                                        .addOnSuccessListener { result ->
                                            val d = result.text.filter { ch -> ch.isDigit() }
                                            if (d.length >= 3) {
                                                val v = d.take(7)
                                                digits = v
                                                onDigits(v)
                                            }
                                        }
                                        .addOnCompleteListener { proxy.close() }
                                } else {
                                    proxy.close()
                                }
                            }
                            provider.unbindAll()
                            try {
                                provider.bindToLifecycle(
                                    lifecycle,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysis
                                )
                            } catch (_: Exception) {
                                Toast.makeText(c, "Kamera gagal dibuka.", Toast.LENGTH_SHORT).show()
                            }
                        }, ContextCompat.getMainExecutor(c))
                        pv
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(260.dp, 110.dp)
                            .border(3.dp, AirBlue, RoundedCornerShape(16.dp))
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Izin kamera diperlukan untuk scan meter.",
                        color = Muted, fontSize = 13.sp,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Hasil scan — bisa dikoreksi manual", fontSize = 11.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = digits,
                    onValueChange = {
                        digits = it.filter { ch -> ch.isDigit() }.take(7)
                        onDigits(digits)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Arahkan kamera ke angka meter…") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Batal") }
                    Button(
                        onClick = onDone,
                        enabled = digits.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                    ) { Text("💾 Simpan", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "OCR berjalan di HP, tanpa internet.",
                    fontSize = 10.sp, color = Muted
                )
            }
        }
    }
}
