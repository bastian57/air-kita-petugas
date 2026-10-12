package com.daftech.airkita.petugas.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daftech.airkita.petugas.data.Repository
import com.daftech.airkita.petugas.data.entity.Customer
import com.daftech.airkita.petugas.ui.components.GradientHeader
import com.daftech.airkita.petugas.ui.components.PetugasBottomBar
import com.daftech.airkita.petugas.ui.nav.Routes
import com.daftech.airkita.petugas.ui.theme.AirBlue
import com.daftech.airkita.petugas.ui.theme.AppBg
import com.daftech.airkita.petugas.ui.theme.CardBg
import com.daftech.airkita.petugas.ui.theme.Green
import com.daftech.airkita.petugas.ui.theme.Muted
import com.daftech.airkita.petugas.ui.theme.Navy

/**
 * Beranda petugas: progres pencatatan + daftar pelanggan + tombol kirim/terima.
 */
@Composable
fun HomeScreen(repo: Repository, nav: NavController) {
    val customers by repo.customers().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var recordedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var pendingCount by remember { mutableStateOf(0) }

    LaunchedEffect(customers) {
        val readings = repo.readingsOnce()
        recordedIds = readings.map { it.customerId }.toSet()
        pendingCount = readings.size
    }

    val filtered = remember(customers, query) {
        if (query.isBlank()) customers
        else customers.filter {
            it.name.contains(query, ignoreCase = true) || it.meterNo.contains(query, ignoreCase = true)
        }
    }
    val done = customers.count { recordedIds.contains(it.id) }

    Scaffold(
        containerColor = AppBg,
        bottomBar = { PetugasBottomBar(nav, Routes.HOME) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            GradientHeader(
                "💧 AIR KITA Petugas",
                if (customers.isEmpty()) "Belum ada data — terima via QR dulu"
                else "${customers.size} pelanggan"
            )
            Column(Modifier.padding(14.dp)) {
                // Progres
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Progres pencatatan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
                            Text("$done / ${customers.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AirBlue)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (customers.isEmpty()) 0f else done.toFloat() / customers.size },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = AirBlue,
                            trackColor = AppBg
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (pendingCount > 0) "$pendingCount catatan siap dikirim"
                            else "Belum ada catatan",
                            fontSize = 11.sp, color = Muted
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                // Aksi
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { nav.navigate(Routes.RECEIVE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("📥 Terima Data") }
                    Button(
                        onClick = { nav.navigate(Routes.SEND) },
                        enabled = pendingCount > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AirBlue)
                    ) { Text("📤 Kirim ($pendingCount)", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(10.dp))
                // Cari
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari nama / no. meter…", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Muted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                // Daftar
                if (customers.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Belum ada daftar pelanggan.\nTap \"Terima Data\" lalu scan QR dari admin.",
                            fontSize = 13.sp, color = Muted,
                            modifier = Modifier.padding(18.dp),
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { c ->
                            CustomerRow(c, recordedIds.contains(c.id)) {
                                nav.navigate(Routes.record(c.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRow(c: Customer, done: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (done) {
                Icon(
                    Icons.Filled.Check, contentDescription = null, tint = CardBg,
                    modifier = Modifier.size(30.dp).clip(CircleShape).background(Green).padding(6.dp)
                )
            } else {
                Spacer(
                    Modifier.size(30.dp).clip(CircleShape)
                        .background(AppBg)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Navy)
                Text(
                    "${c.meterNo} • ${c.lastMeter} m³",
                    fontSize = 12.sp, color = Muted
                )
            }
            Text("›", fontSize = 20.sp, color = Muted)
        }
    }
}
