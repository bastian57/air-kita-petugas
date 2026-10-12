package com.daftech.airkita.petugas

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.daftech.airkita.petugas.license.LicenseManager
import com.daftech.airkita.petugas.ui.nav.PetugasNav
import com.daftech.airkita.petugas.ui.screen.ActivationScreen
import com.daftech.airkita.petugas.ui.theme.PetugasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as PetugasApp).repository
        setContent {
            PetugasTheme {
                var activated by remember {
                    val prefs = getSharedPreferences(
                        LicenseManager.PREFS_NAME, Context.MODE_PRIVATE
                    )
                    mutableStateOf(LicenseManager.isActivated(prefs))
                }
                if (activated) {
                    PetugasNav(repo = repo)
                } else {
                    ActivationScreen(onActivated = { activated = true })
                }
            }
        }
    }
}
