package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpeedDial

@Composable
fun HomeScreen(
  uiState: HomeUiState,
  onSearchQueryChange: (String) -> Unit,
  onClearSearch: () -> Unit,
  onSearchSubmit: () -> Unit,
  onSpeedDialClick: (SpeedDial) -> Unit,
  onSpeedDialLongClick: (SpeedDial) -> Unit,
  onDismissMenu: () -> Unit,
  onEditClick: (SpeedDial) -> Unit,
  onDeleteClick: (SpeedDial) -> Unit,
  onRenameClick: (SpeedDial) -> Unit,
  onChangeWebsiteClick: (SpeedDial) -> Unit,
  onAddClick: () -> Unit,
  onSelectBottomTab: (String) -> Unit,
  onConfirmAdd: (String, String) -> Unit,
  onConfirmRename: (String, String) -> Unit,
  onConfirmChangeWebsite: (String, String) -> Unit,
  onDismissDialog: () -> Unit,
  onClearFeedback: () -> Unit,
  onWebUrlChange: (String) -> Unit,
  onWebTitleChange: (String) -> Unit,
  onCloseWebView: () -> Unit,
  onSelectTabItem: (String) -> Unit,
  onCloseTab: (String) -> Unit,
  onAddNewTab: () -> Unit,
  onDismissTabMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val focusManager = LocalFocusManager.current

  LaunchedEffect(uiState.feedbackMessage) {
    uiState.feedbackMessage?.let {
      snackbarHostState.showSnackbar(it)
      onClearFeedback()
    }
  }

  // Dismiss Tab menu on back press if open
  BackHandler(enabled = uiState.isTabMenuOpen) {
    onDismissTabMenu()
  }

  Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        // Top search bar is ONLY shown on home screen (when no active website URL is open)
        if (uiState.activeUrl == null) {
          TopSearchBarSection(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onClearSearch = onClearSearch,
            onSearchSubmit = {
              focusManager.clearFocus()
              onSearchSubmit()
            }
          )
        }
      },
      bottomBar = {
        // Narrow toolbar for both home screen and full-screen web view
        NarrowBottomToolbar(
          activeTab = if (uiState.isTabMenuOpen) "tabs" else if (uiState.activeUrl == null) "home" else "web",
          isInWebView = uiState.activeUrl != null,
          tabCount = uiState.tabs.size,
          onSelectTab = onSelectBottomTab
        )
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (uiState.activeUrl != null) {
          // Full screen Google WebView with only notificationbar at top and narrow toolbar at bottom
          BrowserWebView(
            url = uiState.activeUrl ?: "",
            onUrlChange = onWebUrlChange,
            onPageTitleChange = onWebTitleChange,
            onClose = onCloseWebView,
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
          )
        } else {
          // Home Screen with auto-aligned Speed Dials grid and plus icon at the end
          SpeedDialGrid(
            speedDials = uiState.speedDials,
            contextMenuState = uiState.contextMenuState,
            onSpeedDialClick = onSpeedDialClick,
            onSpeedDialLongClick = onSpeedDialLongClick,
            onDismissMenu = onDismissMenu,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onRenameClick = onRenameClick,
            onChangeWebsiteClick = onChangeWebsiteClick,
            onAddClick = onAddClick,
            modifier = Modifier.fillMaxSize()
          )
        }
        // Half Screen Tab Menu (placed inside Scaffold content area so the toolbar stays visible!)
        TabMenuSheet(
          isOpen = uiState.isTabMenuOpen,
          tabs = uiState.tabs,
          activeTabId = uiState.activeTabId,
          onSelectTab = onSelectTabItem,
          onCloseTab = onCloseTab,
          onAddNewTab = onAddNewTab,
          onDismiss = onDismissTabMenu,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Active Dialogs
      when (val dialog = uiState.activeDialog) {
        is ActiveDialog.Add -> {
          AddSpeedDialDialog(
            onDismiss = onDismissDialog,
            onConfirm = onConfirmAdd
          )
        }
        is ActiveDialog.Rename -> {
          RenameSpeedDialDialog(
            speedDial = dialog.speedDial,
            onDismiss = onDismissDialog,
            onConfirm = onConfirmRename
          )
        }
        is ActiveDialog.ChangeWebsite -> {
          ChangeWebsiteDialog(
            speedDial = dialog.speedDial,
            onDismiss = onDismissDialog,
            onConfirm = onConfirmChangeWebsite
          )
        }
        ActiveDialog.None -> Unit
      }
    }
  }
}

/**
 * Top Search Bar with all text above removed.
 */
@Composable
fun TopSearchBarSection(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onClearSearch: () -> Unit,
  onSearchSubmit: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
          Text(
            text = "Search or type web address",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        leadingIcon = {
          IconButton(
            onClick = onSearchSubmit,
            modifier = Modifier.testTag("search_icon_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = onClearSearch,
              modifier = Modifier.testTag("clear_search_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Uri,
          imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("top_search_bar")
      )
    }
  }
}

/**
 * Narrow Toolbar:
 * - Sleek 48dp height
 * - Clean icon-only items: Add to speed dials, Home, Tabs
 * - Accurate tab count badge
 */
@Composable
fun NarrowBottomToolbar(
  activeTab: String,
  isInWebView: Boolean,
  tabCount: Int,
  onSelectTab: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 3.dp,
    shadowElevation = 3.dp,
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("bottom_menu_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp) // Narrow toolbar
        .padding(horizontal = 24.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Add to speed dials
      IconButton(
        onClick = { onSelectTab("add_to_speed_dials") },
        modifier = Modifier
          .size(48.dp)
          .testTag("menu_item_add_speed_dial")
      ) {
        Icon(
          imageVector = AppIcons.BookmarkAdd,
          contentDescription = "Add to speed dials",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      // 2. Home
      IconButton(
        onClick = { onSelectTab("home") },
        modifier = Modifier
          .size(48.dp)
          .testTag("menu_item_home")
      ) {
        Icon(
          imageVector = Icons.Default.Home,
          contentDescription = "Home",
          tint = if (activeTab == "home" && !isInWebView) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.size(24.dp)
        )
      }

      // 3. Tabs (Square box showing tab count inside)
      IconButton(
        onClick = { onSelectTab("tabs") },
        modifier = Modifier
          .size(48.dp)
          .testTag("menu_item_tabs")
      ) {
        val tabColor = if (activeTab == "tabs") {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        }

        Box(
          modifier = Modifier
            .size(20.dp)
            .border(
              width = 1.8.dp,
              color = tabColor,
              shape = RoundedCornerShape(5.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (tabCount > 99) ":D" else "$tabCount",
            fontSize = if (tabCount > 9) 9.5.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = tabColor
          )
        }
      }
    }
  }
}
