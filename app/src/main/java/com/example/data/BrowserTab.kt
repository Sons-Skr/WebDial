package com.example.data

import java.util.UUID

data class BrowserTab(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "Home",
  val url: String? = null // null indicates Home screen with Speed Dials
) {
  val isHome: Boolean
    get() = url.isNullOrBlank()

  val domain: String
    get() {
      val targetUrl = url ?: return "Home"
      return targetUrl.trim()
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .split("/")
        .firstOrNull() ?: targetUrl
    }

  val faviconUrl: String
    get() = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
}
