package com.example

import com.example.data.BrowserTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `home tab has null url and isHome is true`() {
    val tab = BrowserTab(title = "Home", url = null)
    assertTrue(tab.isHome)
    assertNull(tab.url)
    assertEquals("Home", tab.domain)
  }

  @Test
  fun `web tab extracts domain correctly`() {
    val tab = BrowserTab(title = "Wikipedia", url = "https://www.wikipedia.org/wiki/Main_Page")
    assertEquals("wikipedia.org", tab.domain)
  }
}
