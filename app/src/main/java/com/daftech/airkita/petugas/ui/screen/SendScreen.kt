package com.daftech.airkita.petugas.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daftech.airkita.petugas.data.Repository
import com.daftech.airkita.petugas.qr.QrBitmap
import com.daftech.airkita.petugas.qr.QrCodec
import com.daftech.airkita.petugas.qr.ReadingPayload
import com.daftech.airkita.petugas.ui.components.GradientHeader
import com.daftech.airkita.petugas.ui.components.PetugasBottomBar
import com.daftech.airkita.petugas.ui.nav.Routes
import com.daftech.airkita.petugas.ui.theme.AirBlue
import com.daftech.airkita.petugas.ui.theme.AppBg
import com.daftech.airkita.petugas.ui.theme.CardBg
import com.daftech.airkita.petugas.ui.theme.Muted
import com.daftech.airkita.petugas.ui.theme.Navy
import kotlinx.coroutines.launch

/**
 * Kirim hasil pencatatan: tampilkan QR (bisa multi-QR) untuk dipindai admin.
 */
@Composable
fun SendScreen(repo: Repository, nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var packets by remember { mutableStateOf<List<String>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val readings = repo.readingsOnce()
        count = readings.size
        packets = if (readings.isEmpty()) emptyList()
        else QrCodec.encodeReadings(
            readings.map { ReadingPayload(it.customerId, it.meterEnd, it.recordedAt) }
        )
        index = 0
    }

    val bitmap = remember(packets, index) {
        packets.getOrNull(index)?.let { QrBitmap.generate(it, 800) }
    }

    Scaffold(
        containerColor = AppBg,
        bottomBar = { PetugasBottomBar(nav, Routes.SEND) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            GradientHeader("Kirim Hasil", "$count catatan siap dikirim", slim = true)
            Column(
                Modifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (packets.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Belum ada catatan.\nCatat meter dulu di Beranda.",
                            fontSize = 13.sp, color = Muted,
                            modifier = Modifier.padding(18.dp),
                            lineHeight = 20.sp, textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Tunjukkan ke admin untuk dipindai",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy
                            )
                            Spacer(Modifier.height(10.dp))
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "QR ${index + 1}/${packets.size}",
                                    modifier = Modifier.size(260.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "QR ${index + 1} dari ${packets.size}",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AirBlue
                            )
                            if (packets.size > 1) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { if (index > 0) index-- },
                                        enabled = index > 0,
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("‹ Sebelumnya") }
                                    OutlinedButton(
                                        onClick = { if (index < packets.size - 1) index++ },
                                        enabled = index < packets.size - 1,
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Berikutnya ›") }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { confirmClear = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                    ) { Text("✅ Tandai Sudah Dikirim", fontWeight = FontWeight.Bold) }
                    Text(
                        "Hapus catatan di HP ini setelah admin berhasil memindai.",
                        fontSize = 11.sp, color = Muted,
                        modifier = Modifier.padding(top = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Hapus catatan?", fontWeight = FontWeight.Bold) },
            text = { Text("Pastikan admin sudah memindai SEMUA QR ($count catatan). Data yang dihapus tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo.clearReadings()
                            confirmClear = false
                            Toast.makeText(ctx, "Catatan dihapus.", Toast.LENGTH_SHORT).show()
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                ) { Text("Ya, Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Batal") }
            }
        )
    }
}
