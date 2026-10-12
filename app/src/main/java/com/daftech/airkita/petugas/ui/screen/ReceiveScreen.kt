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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.daftech.airkita.petugas.data.Repository
import com.daftech.airkita.petugas.data.entity.Customer
import com.daftech.airkita.petugas.qr.QrCodec
import com.daftech.airkita.petugas.qr.QrPacket
import com.daftech.airkita.petugas.ui.components.GradientHeader
import com.daftech.airkita.petugas.ui.components.PetugasBottomBar
import com.daftech.airkita.petugas.ui.nav.Routes
import com.daftech.airkita.petugas.ui.theme.AirBlue
import com.daftech.airkita.petugas.ui.theme.AppBg
import com.daftech.airkita.petugas.ui.theme.CardBg
import com.daftech.airkita.petugas.ui.theme.Green
import com.daftech.airkita.petugas.ui.theme.Muted
import com.daftech.airkita.petugas.ui.theme.Navy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

/**
 * Terima data pelanggan dari admin via scan QR (multi-QR otomatis).
 */
@Composable
fun ReceiveScreen(repo: Repository, nav: NavController) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

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

    var packets by remember { mutableStateOf<Map<Int, QrPacket>>(emptyMap()) }
    var packetType by remember { mutableStateOf<String?>(null) }
    var expectedTotal by remember { mutableIntStateOf(0) }
    var previewList by remember { mutableStateOf<List<String>>(emptyList()) }
    var saving by remember { mutableStateOf(false) }

    val complete = expectedTotal > 0 && packets.size >= expectedTotal

    val scanner = remember { BarcodeScanning.getClient() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var providerRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            providerRef?.unbindAll()
            cameraExecutor.shutdown()
            try { scanner.close() } catch (_: Exception) {}
        }
    }

    fun handleRaw(raw: String) {
        val p = QrCodec.parsePacket(raw) ?: return
        if (p.type != "C") return // layar ini hanya terima daftar pelanggan
        if (packetType != null && packetType != p.type) return
        packetType = p.type
        expectedTotal = p.total
        if (!packets.containsKey(p.part)) {
            packets = packets + (p.part to p)
        }
    }

    fun save() {
        val json = QrCodec.combine(packets.values) ?: run {
            Toast.makeText(ctx, "Data belum lengkap.", Toast.LENGTH_SHORT).show()
            return
        }
        saving = true
        scope.launch {
            try {
                val payloads = QrCodec.parseCustomers(json)
                repo.clearCustomers()
                repo.clearReadings()
                repo.addCustomers(payloads.map {
                    Customer(
                        id = it.id,
                        name = it.name,
                        meterNo = it.meterNo,
                        address = it.address,
                        lastMeter = it.lastMeter
                    )
                })
                Toast.makeText(ctx, "${payloads.size} pelanggan tersimpan.", Toast.LENGTH_LONG).show()
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                saving = false
            }
        }
    }

    // Pratinjau nama pelanggan setelah lengkap
    LaunchedEffect(complete) {
        if (complete) {
            val json = QrCodec.combine(packets.values)
            previewList = try {
                if (json != null) QrCodec.parseCustomers(json).take(5).map { it.name } else emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    Scaffold(
        containerColor = AppBg,
        bottomBar = { PetugasBottomBar(nav, Routes.RECEIVE) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            GradientHeader("Terima Data", "Scan QR dari admin", slim = true)
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
                                    if (now - lastTs > 500 && media != null && !complete) {
                                        lastTs = now
                                        val image = InputImage.fromMediaImage(
                                            media, proxy.imageInfo.rotationDegrees
                                        )
                                        scanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                for (b in barcodes) {
                                                    b.rawValue?.let { handleRaw(it) }
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
                                .size(240.dp)
                                .border(3.dp, AirBlue, RoundedCornerShape(16.dp))
                        )
                    }
                    if (expectedTotal > 0) {
                        Box(
                            Modifier.fillMaxWidth().padding(14.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Text(
                                    "Terkumpul ${packets.size}/$expectedTotal QR",
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Izin kamera diperlukan untuk scan QR.",
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
                    if (expectedTotal == 0) {
                        Text(
                            "Arahkan kamera ke QR yang ditampilkan admin.\nQR ganda akan terbaca otomatis berurutan.",
                            fontSize = 12.sp, color = Muted, lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                        )
                    } else if (!complete) {
                        Text(
                            "Memindai… ${packets.size}/$expectedTotal",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { packets.size.toFloat() / expectedTotal },
                            modifier = Modifier.fillMaxWidth(),
                            color = AirBlue
                        )
                    } else {
                        Text(
                            "✅ Lengkap! ${packets.size} QR terbaca.",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                        )
                        if (previewList.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Contoh: " + previewList.joinToString(", "),
                                fontSize = 11.sp, color = Muted,
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { save() },
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                        ) { Text(if (saving) "Menyimpan…" else "💾 Simpan ke HP", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
