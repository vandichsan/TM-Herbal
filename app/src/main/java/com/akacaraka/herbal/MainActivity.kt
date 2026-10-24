package com.akacaraka.herbal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akacaraka.herbal.ui.HerbalApp
import com.akacaraka.herbal.ui.HerbalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: HerbalViewModel = viewModel()
            HerbalApp(viewModel)
        }
    }
}
