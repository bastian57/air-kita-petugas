package com.daftech.airkita.petugas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.daftech.airkita.petugas.ui.nav.PetugasNav
import com.daftech.airkita.petugas.ui.theme.PetugasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as PetugasApp).repository
        setContent {
            PetugasTheme {
                PetugasNav(repo = repo)
            }
        }
    }
}
