package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.BrowserTab
import com.example.data.SpeedDial
import com.example.data.SpeedDialRepository
import java.net.URLEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface ContextMenuState {
  data object None : ContextMenuState
  data class MainMenu(val speedDial: SpeedDial) : ContextMenuState
  data class EditSubMenu(val speedDial: SpeedDial) : ContextMenuState
}

sealed interface ActiveDialog {
  data object None : ActiveDialog
  data class Add(val initialTitle: String = "", val initialUrl: String = "") : ActiveDialog
  data class Rename(val speedDial: SpeedDial) : ActiveDialog
  data class ChangeWebsite(val speedDial: SpeedDial) : ActiveDialog
}

data class HomeUiState(
  val speedDials: List<SpeedDial> = emptyList(),
  val searchQuery: String = "",
  val tabs: List<BrowserTab> = listOf(BrowserTab(title = "Home", url = null)),
  val activeTabId: String = "",
  val isTabMenuOpen: Boolean = false,
  val contextMenuState: ContextMenuState = ContextMenuState.None,
  val activeDialog: ActiveDialog = ActiveDialog.None,
  val feedbackMessage: String? = null
) {
  val currentTab: BrowserTab
    get() = tabs.firstOrNull { it.id == activeTabId } ?: tabs.firstOrNull() ?: BrowserTab(title = "Home", url = null)

  val activeUrl: String?
    get() = currentTab.url
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = SpeedDialRepository(application)

  private val initialTab = BrowserTab(title = "Home", url = null)
  private val _uiState = MutableStateFlow(
    HomeUiState(
      tabs = listOf(initialTab),
      activeTabId = initialTab.id
    )
  )
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    loadSpeedDials()
  }

  private fun loadSpeedDials() {
    val items = repository.loadSpeedDials()
    _uiState.update { it.copy(speedDials = items) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun clearSearchQuery() {
    _uiState.update { it.copy(searchQuery = "") }
  }

  fun submitSearch() {
    val query = _uiState.value.searchQuery.trim()
    if (query.isNotEmpty()) {
      val targetUrl = parseInputToUrl(query)
      updateActiveTabUrl(targetUrl, query)
    }
  }

  fun onSpeedDialClick(speedDial: SpeedDial) {
    val targetUrl = speedDial.url
    updateActiveTabUrl(targetUrl, speedDial.title)
  }

  private fun updateActiveTabUrl(newUrl: String, title: String) {
    val currentTabId = _uiState.value.activeTabId
    val updatedTabs = _uiState.value.tabs.map { tab ->
      if (tab.id == currentTabId) {
        tab.copy(url = newUrl, title = title)
      } else {
        tab
      }
    }
    _uiState.update {
      it.copy(
        tabs = updatedTabs,
        searchQuery = newUrl,
        isTabMenuOpen = false
      )
    }
  }

  fun onSpeedDialLongClick(speedDial: SpeedDial) {
    _uiState.update {
      it.copy(contextMenuState = ContextMenuState.MainMenu(speedDial))
    }
  }

  fun onDismissMenu() {
    _uiState.update { it.copy(contextMenuState = ContextMenuState.None) }
  }

  fun onEditFromMenu(speedDial: SpeedDial) {
    _uiState.update {
      it.copy(contextMenuState = ContextMenuState.EditSubMenu(speedDial))
    }
  }

  fun onDeleteFromMenu(speedDial: SpeedDial) {
    val remaining = _uiState.value.speedDials.filter { it.id != speedDial.id }
    val aligned = repository.saveSpeedDials(remaining)
    _uiState.update {
      it.copy(
        speedDials = aligned,
        contextMenuState = ContextMenuState.None,
        feedbackMessage = "Deleted ${speedDial.title}"
      )
    }
  }

  fun onSelectRename(speedDial: SpeedDial) {
    _uiState.update {
      it.copy(
        contextMenuState = ContextMenuState.None,
        activeDialog = ActiveDialog.Rename(speedDial)
      )
    }
  }

  fun onSelectChangeWebsite(speedDial: SpeedDial) {
    _uiState.update {
      it.copy(
        contextMenuState = ContextMenuState.None,
        activeDialog = ActiveDialog.ChangeWebsite(speedDial)
      )
    }
  }

  fun openAddSpeedDial(initialTitle: String = "", initialUrl: String = "") {
    _uiState.update {
      it.copy(
        contextMenuState = ContextMenuState.None,
        activeDialog = ActiveDialog.Add(initialTitle = initialTitle, initialUrl = initialUrl)
      )
    }
  }

  fun dismissDialog() {
    _uiState.update { it.copy(activeDialog = ActiveDialog.None) }
  }

  fun clearFeedbackMessage() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  fun confirmAdd(title: String, url: String) {
    val cleanUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
      url.trim()
    } else {
      "https://${url.trim()}"
    }

    val cleanTitle = title.trim().ifEmpty {
      cleanUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.").split("/").firstOrNull()?.split(".")?.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Website"
    }

    val currentList = _uiState.value.speedDials
    val normalizedClean = cleanUrl.trimEnd('/')
    val alreadyExists = currentList.any { it.url.trim().trimEnd('/') == normalizedClean }
    if (alreadyExists) {
      _uiState.update {
        it.copy(
          activeDialog = ActiveDialog.None,
          feedbackMessage = "$cleanTitle is already in speed dials"
        )
      }
      return
    }

    val newItem = SpeedDial(
      title = cleanTitle,
      url = cleanUrl,
      position = currentList.size
    )

    val updated = repository.saveSpeedDials(currentList + newItem)

    _uiState.update {
      it.copy(
        speedDials = updated,
        activeDialog = ActiveDialog.None,
        feedbackMessage = "Added $cleanTitle to speed dials"
      )
    }
  }

  fun confirmRename(speedDialId: String, newTitle: String) {
    val cleanTitle = newTitle.trim().ifEmpty { "Website" }
    val updated = _uiState.value.speedDials.map {
      if (it.id == speedDialId) it.copy(title = cleanTitle) else it
    }
    val aligned = repository.saveSpeedDials(updated)
    _uiState.update {
      it.copy(
        speedDials = aligned,
        activeDialog = ActiveDialog.None,
        feedbackMessage = "Renamed to $cleanTitle"
      )
    }
  }

  fun confirmChangeWebsite(speedDialId: String, newUrl: String) {
    val cleanUrl = if (newUrl.startsWith("http://") || newUrl.startsWith("https://")) {
      newUrl.trim()
    } else {
      "https://${newUrl.trim()}"
    }
    val updated = _uiState.value.speedDials.map {
      if (it.id == speedDialId) it.copy(url = cleanUrl) else it
    }
    val aligned = repository.saveSpeedDials(updated)
    _uiState.update {
      it.copy(
        speedDials = aligned,
        activeDialog = ActiveDialog.None,
        feedbackMessage = "Website updated to $cleanUrl"
      )
    }
  }

  // --- TAB MANAGEMENT ---

  fun openTabMenu() {
    _uiState.update { it.copy(isTabMenuOpen = true) }
  }

  fun closeTabMenu() {
    _uiState.update { it.copy(isTabMenuOpen = false) }
  }

  fun selectTab(tabId: String) {
    val targetTab = _uiState.value.tabs.firstOrNull { it.id == tabId }
    if (targetTab != null) {
      _uiState.update {
        it.copy(
          activeTabId = tabId,
          searchQuery = targetTab.url ?: "",
          isTabMenuOpen = false
        )
      }
    }
  }

  fun addNewTab() {
    val newTab = BrowserTab(title = "Home", url = null)
    _uiState.update {
      it.copy(
        tabs = it.tabs + newTab,
        activeTabId = newTab.id,
        searchQuery = "",
        isTabMenuOpen = false
      )
    }
  }

  fun closeTab(tabId: String) {
    val currentTabs = _uiState.value.tabs
    val remainingTabs = currentTabs.filter { it.id != tabId }

    if (remainingTabs.isEmpty()) {
      // "When close all tabs it automatically open a new tab of home page"
      val homeTab = BrowserTab(title = "Home", url = null)
      _uiState.update {
        it.copy(
          tabs = listOf(homeTab),
          activeTabId = homeTab.id,
          searchQuery = ""
        )
      }
    } else {
      // If we closed the active tab, switch to the last remaining tab
      val nextActiveTabId = if (_uiState.value.activeTabId == tabId) {
        remainingTabs.last().id
      } else {
        _uiState.value.activeTabId
      }
      val nextTab = remainingTabs.firstOrNull { it.id == nextActiveTabId } ?: remainingTabs.last()
      _uiState.update {
        it.copy(
          tabs = remainingTabs,
          activeTabId = nextActiveTabId,
          searchQuery = nextTab.url ?: ""
        )
      }
    }
  }

  fun onSelectBottomTab(tabId: String) {
    when (tabId) {
      "add_to_speed_dials" -> {
        val currentTab = _uiState.value.currentTab
        val currentUrl = currentTab.url
        if (!currentUrl.isNullOrBlank()) {
          // When a website is open, automatically add it to speed dials
          val currentTitle = currentTab.title.trim().ifEmpty {
            currentUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.").split("/").firstOrNull() ?: "Website"
          }
          confirmAdd(currentTitle, currentUrl)
        } else {
          openAddSpeedDial()
        }
      }
      "home" -> {
        // Closes the website and opens home page on current tab
        val currentTabId = _uiState.value.activeTabId
        val updatedTabs = _uiState.value.tabs.map { tab ->
          if (tab.id == currentTabId) {
            tab.copy(title = "Home", url = null)
          } else {
            tab
          }
        }
        _uiState.update {
          it.copy(
            tabs = updatedTabs,
            searchQuery = "",
            isTabMenuOpen = false
          )
        }
      }
      "tabs" -> {
        // Toggle half-screen tab menu
        _uiState.update { it.copy(isTabMenuOpen = !it.isTabMenuOpen) }
      }
    }
  }

  fun onWebUrlChanged(newUrl: String) {
    val currentTabId = _uiState.value.activeTabId
    val updatedTabs = _uiState.value.tabs.map { tab ->
      if (tab.id == currentTabId) {
        tab.copy(url = newUrl)
      } else {
        tab
      }
    }
    _uiState.update {
      it.copy(
        tabs = updatedTabs,
        searchQuery = newUrl
      )
    }
  }

  fun onWebTitleChanged(newTitle: String) {
    val currentTabId = _uiState.value.activeTabId
    val updatedTabs = _uiState.value.tabs.map { tab ->
      if (tab.id == currentTabId) {
        tab.copy(title = newTitle)
      } else {
        tab
      }
    }
    _uiState.update {
      it.copy(tabs = updatedTabs)
    }
  }

  fun closeWebView() {
    val currentTabId = _uiState.value.activeTabId
    val updatedTabs = _uiState.value.tabs.map { tab ->
      if (tab.id == currentTabId) {
        tab.copy(title = "Home", url = null)
      } else {
        tab
      }
    }
    _uiState.update {
      it.copy(
        tabs = updatedTabs,
        searchQuery = ""
      )
    }
  }

  private fun parseInputToUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""

    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
      return trimmed
    }

    val hasNoSpaces = !trimmed.contains(" ")
    val hasDot = trimmed.contains(".")
    if (hasNoSpaces && hasDot) {
      return "https://$trimmed"
    }

    return try {
      val encoded = URLEncoder.encode(trimmed, "UTF-8")
      "https://www.google.com/search?q=$encoded"
    } catch (e: Exception) {
      "https://www.google.com/search?q=$trimmed"
    }
  }
}
