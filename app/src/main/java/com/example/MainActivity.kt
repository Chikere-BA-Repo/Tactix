package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.TactixDatabase
import com.example.data.TactixRepository
import com.example.ui.TactixApp
import com.example.ui.theme.TactixTheme
import com.example.ui.util.HapticsController
import com.example.ui.viewmodel.ArchiveViewModel
import com.example.ui.viewmodel.DailyViewModel
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.TactixViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TactixDatabase.getInstance(applicationContext)
        val repository = TactixRepository(database.tactixDao())
        val haptics = HapticsController(applicationContext)
        val factory = TactixViewModelFactory(repository, haptics)

        val gameViewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]
        val dailyViewModel = ViewModelProvider(this, factory)[DailyViewModel::class.java]
        val archiveViewModel = ViewModelProvider(this, factory)[ArchiveViewModel::class.java]

        setContent {
            TactixTheme {
                TactixApp(
                    gameViewModel = gameViewModel,
                    dailyViewModel = dailyViewModel,
                    archiveViewModel = archiveViewModel,
                )
            }
        }
    }
}
