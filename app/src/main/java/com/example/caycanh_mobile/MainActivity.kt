package com.example.caycanh_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.caycanh_mobile.ui.navigation.AppNavGraph
import com.example.caycanh_mobile.ui.theme.CayCanhTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CayCanhTheme {
                AppNavGraph()
            }
        }
    }
}