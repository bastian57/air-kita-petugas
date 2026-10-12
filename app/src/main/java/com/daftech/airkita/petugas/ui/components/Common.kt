package com.daftech.airkita.petugas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daftech.airkita.petugas.ui.nav.Routes
import com.daftech.airkita.petugas.ui.theme.AirBlue
import com.daftech.airkita.petugas.ui.theme.CardBg
import com.daftech.airkita.petugas.ui.theme.Muted
import com.daftech.airkita.petugas.ui.theme.Navy

/** Header gradien biru air. */
@Composable
fun GradientHeader(
    title: String,
    subtitle: String,
    slim: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Navy, AirBlue)))
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = if (slim) 30.dp else 46.dp)
    ) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

/** Bottom bar 3 tab: Beranda, Terima QR, Kirim QR. */
@Composable
fun PetugasBottomBar(nav: NavController, current: String) {
    val tabs = listOf(
        Tab(Routes.HOME, "Beranda", Icons.Filled.Home),
        Tab(Routes.RECEIVE, "Terima", Icons.Filled.QrCodeScanner),
        Tab(Routes.SEND, "Kirim", Icons.Filled.QrCode)
    )
    NavigationBar(containerColor = CardBg) {
        tabs.forEach { t ->
            NavigationBarItem(
                selected = current == t.route,
                onClick = {
                    if (current != t.route) {
                        nav.navigate(t.route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(t.icon, contentDescription = t.label) },
                label = { Text(t.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AirBlue,
                    selectedTextColor = AirBlue,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    indicatorColor = AirBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/** Baris info kecil dua kolom. */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Navy)
    }
}
