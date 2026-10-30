package com.example

import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import com.example.ui.HomeScreen
import com.example.ui.HomeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val viewModel: HomeViewModel = viewModel()
          val uiState by viewModel.uiState.collectAsStateWithLifecycle()

          // When on root home screen with no web view and no tab menu, exit cleanly
          BackHandler(enabled = uiState.activeUrl == null && !uiState.isTabMenuOpen) {
            finishAndRemoveTask()
          }

          HomeScreen(
            uiState = uiState,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onClearSearch = viewModel::clearSearchQuery,
            onSearchSubmit = viewModel::submitSearch,
            onSpeedDialClick = viewModel::onSpeedDialClick,
            onSpeedDialLongClick = viewModel::onSpeedDialLongClick,
            onDismissMenu = viewModel::onDismissMenu,
            onEditClick = viewModel::onEditFromMenu,
            onDeleteClick = viewModel::onDeleteFromMenu,
            onRenameClick = viewModel::onSelectRename,
            onChangeWebsiteClick = viewModel::onSelectChangeWebsite,
            onAddClick = { viewModel.openAddSpeedDial() },
            onSelectBottomTab = viewModel::onSelectBottomTab,
            onConfirmAdd = viewModel::confirmAdd,
            onConfirmRename = viewModel::confirmRename,
            onConfirmChangeWebsite = viewModel::confirmChangeWebsite,
            onDismissDialog = viewModel::dismissDialog,
            onClearFeedback = viewModel::clearFeedbackMessage,
            onWebUrlChange = viewModel::onWebUrlChanged,
            onWebTitleChange = viewModel::onWebTitleChanged,
            onCloseWebView = viewModel::closeWebView,
            onSelectTabItem = viewModel::selectTab,
            onCloseTab = viewModel::closeTab,
            onAddNewTab = viewModel::addNewTab,
            onDismissTabMenu = viewModel::closeTabMenu
          )
        }
      }
    }
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    // Release image caches immediately to keep RAM usage minimal
    if (level >= TRIM_MEMORY_UI_HIDDEN) {
      Coil.imageLoader(this).memoryCache?.clear()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    // Terminate process when user closes the app so it never runs or lingers in the background
    Process.killProcess(Process.myPid())
  }
}
