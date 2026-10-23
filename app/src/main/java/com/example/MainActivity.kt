package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.FrameonTheme
import com.example.ui.theme.ObsidianDeep
import com.example.viewmodel.EditorViewModel

enum class AppDestination {
    HOME,
    EDITOR
}

class MainActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FrameonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianDeep
                ) {
                    FrameonApp(viewModel = editorViewModel)
                }
            }
        }
    }
}

@Composable
fun FrameonApp(viewModel: EditorViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppScreenTransition"
    ) { destination ->
        when (destination) {
            AppDestination.HOME -> {
                HomeScreen(
                    onOpenNewVideo = { uri ->
                        viewModel.loadVideo(uri)
                        currentDestination = AppDestination.EDITOR
                    },
                    onOpenExistingProject = { project ->
                        viewModel.loadExistingProject(project)
                        currentDestination = AppDestination.EDITOR
                    }
                )
            }
            AppDestination.EDITOR -> {
                EditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        currentDestination = AppDestination.HOME
                    }
                )
            }
        }
    }
}
