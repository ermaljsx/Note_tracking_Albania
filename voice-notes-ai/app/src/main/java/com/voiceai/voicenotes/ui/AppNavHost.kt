package com.voiceai.voicenotes.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voiceai.voicenotes.R
import com.voiceai.voicenotes.ui.screens.MainRecordScreen
import com.voiceai.voicenotes.ui.screens.NotesListScreen
import com.voiceai.voicenotes.ui.screens.SettingsScreen
import com.voiceai.voicenotes.ui.screens.TaskListScreen

private enum class Dest(val route: String, val labelRes: Int) {
    RECORD("record", android.R.string.copy),
    NOTES("notes", com.voiceai.voicenotes.R.string.notes),
    TASKS("tasks", com.voiceai.voicenotes.R.string.tasks),
    SETTINGS("settings", com.voiceai.voicenotes.R.string.settings)
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Dest.values().forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(id = android.R.drawable.ic_btn_speak_now), contentDescription = null) },
                        label = { Text(text = stringResource(id = dest.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.RECORD.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Dest.RECORD.route) { MainRecordScreen() }
            composable(Dest.NOTES.route) { NotesListScreen() }
            composable(Dest.TASKS.route) { TaskListScreen() }
            composable(Dest.SETTINGS.route) { SettingsScreen() }
        }
    }
}