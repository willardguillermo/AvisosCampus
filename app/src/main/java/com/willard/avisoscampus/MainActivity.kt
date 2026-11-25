package com.willard.avisoscampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.willard.avisoscampus.data.AvisosRepositoryMemoria
import com.willard.avisoscampus.ui.PantallaAvisos
import com.willard.avisoscampus.ui.theme.AvisosCampusTheme
import com.willard.avisoscampus.viewmodel.AvisosViewModel
import com.willard.avisoscampus.viewmodel.AvisosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvisosCampusTheme {
                val vm: AvisosViewModel = viewModel(
                    factory = AvisosViewModelFactory(AvisosRepositoryMemoria())
                )
                PantallaAvisos(vm)
            }
        }
    }
}