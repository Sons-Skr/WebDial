package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrowserTab
import com.example.data.SpeedDialRepository
import com.example.ui.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WebDial", appName)
  }

  @Test
  fun `test speed dial repository default items`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = SpeedDialRepository(context)
    val dials = repository.loadSpeedDials()
    assertTrue(dials.isNotEmpty())
    assertTrue(dials.any { it.title.equals("YouTube", ignoreCase = true) })
    assertTrue(dials.any { it.title.equals("Google", ignoreCase = true) })
  }

  @Test
  fun `test tab management add and close operations`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeViewModel(application)

    // Initial state: 1 home tab
    assertEquals(1, viewModel.uiState.value.tabs.size)
    assertTrue(viewModel.uiState.value.currentTab.isHome)
    assertNull(viewModel.uiState.value.activeUrl)

    // Add new tab
    viewModel.addNewTab()
    assertEquals(2, viewModel.uiState.value.tabs.size)

    // Close one tab
    val currentActiveId = viewModel.uiState.value.activeTabId
    viewModel.closeTab(currentActiveId)
    assertEquals(1, viewModel.uiState.value.tabs.size)

    // Closing the only tab should automatically recreate a clean Home tab
    viewModel.closeTab(viewModel.uiState.value.activeTabId)
    assertEquals(1, viewModel.uiState.value.tabs.size)
    assertTrue(viewModel.uiState.value.currentTab.isHome)
  }

  @Test
  fun `test search submission formats search engine url`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeViewModel(application)

    viewModel.updateSearchQuery("android jetpack compose")
    viewModel.submitSearch()

    assertNotNull(viewModel.uiState.value.activeUrl)
    assertTrue(viewModel.uiState.value.activeUrl!!.contains("google.com/search"))
  }

  @Test
  fun `test search submission handles direct domain url`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeViewModel(application)

    viewModel.updateSearchQuery("example.com")
    viewModel.submitSearch()

    assertEquals("https://example.com", viewModel.uiState.value.activeUrl)
  }

  @Test
  fun `test toolbar home action closes webview`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeViewModel(application)

    viewModel.updateSearchQuery("example.com")
    viewModel.submitSearch()
    assertNotNull(viewModel.uiState.value.activeUrl)

    // Press home on bottom toolbar
    viewModel.onSelectBottomTab("home")
    assertNull(viewModel.uiState.value.activeUrl)
    assertTrue(viewModel.uiState.value.currentTab.isHome)
  }
}
