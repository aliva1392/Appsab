package com.aistudio.sublimationerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aistudio.sublimationerp.ui.navigation.MainScreen
import com.aistudio.sublimationerp.ui.theme.MyApplicationTheme
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as SublimationApp
    val viewModel: SublimationViewModel by viewModels {
        SublimationViewModelFactory(app.repository)
    }

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
